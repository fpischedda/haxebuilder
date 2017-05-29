import asyncio
import uvloop


asyncio.set_event_loop_policy(uvloop.EventLoopPolicy())

import aioredis
import motor.motor_asyncio
from sanic import Sanic
from sanic.response import json
import auth
from builderlib import jobs
from builderlib import users
from builderlib import repositories
from utils import error_reason


app = Sanic("haxebuilder")
app.config.from_envvar("HAXEBUILDER_CONFIG")

@app.listener("before_server_start")
async def init_db(app, loop):
    client = motor.motor_asyncio.AsyncIOMotorClient(app.config.MONGO_URL)
    app.db = client[app.config.MONGO_DB_NAME]
    app.redis_pool = await aioredis.create_pool(
            (app.config.REDIS_HOST, app.config.REDIS_PORT))
    app.pubsub_channel = app.config.REDIS_PUBSUB_CHANNEL


def get_user_from_request(request):
    # try to get a token from the X-Auth-Token header
    token = request.headers.get("X-Auth-Token", None)

    # if it's empy try to fetch it from the cookies
    if token is None:
        token = request.cookies.get("jwt_token", None)

    if token is None:
        return None
    else:
        return auth.user_from_token(
                token,
                request.app.config.SESSION_SECRET)


@app.route("/login", methods=["POST"])
async def login(request):
    db = request.app.db
    try:
        username = request.form.get("username")
        password = request.form.get("password")

        user = await auth.login(db, username, password)
        token = auth.generate_token(user, request.app.config.SESSION_SECRET)
        user["token"] = token
        response = json(user)
        response.cookies["token"] = token
        return response 
    except users.UserExistsException:
        return error_reason("user already exists")


@app.route("/users/new", methods=["POST"])
async def user_new(request):
    db = request.app.db
    try:
        user = await users.create(db, **request.json)
        return json(user)
    except users.UserExistsException:
        return error_reason("user already exists")


@app.route("/profile", methods=["GET"])
async def user_profile(request):
    db = request.app.db
    request_user = get_user_from_request(request)
    user_id = request_user["_id"]
    user = await users.get_by_id(db, user_id)
    if user is None:
        return error_reason(f"user {user_id} not found")
    user["repositories"] = await repositories.get_all_by_user_id(db, user_id) 
    return json(user)


@app.route("/repositories/new", methods=["POST"])
async def repository_new(request):
    db = request.app.db
    src_repo = validate_repo(request.json)
    repo = await repositories.create(db, **src_repo)
    if repo is None:
        return error_reason("unable to create new repository")
    return json(repo)


@app.route("/repositories/<repo_id>", methods=["GET"])
async def repository_details(request, repo_id):
    db = request.app.db
    repo = await repositories.get_by_id(db, repo_id)
    if repo is None:
        return error_reason(f"unable to find repository {repo_id}")
    return json(repo)


@app.route("/repositories/<repo_id>/build", methods=["POST"])
async def build(request, repo_id):
    db = request.app.db

    repo = await repositories.get_by_id(db, repo_id)
    if repo is None:
        return error_reason(f"repository {repo_id} not found")

    branch = get_branch_from_request(request)
    if branch not in repo["tracked_branches"]:
        return error_reason(f"branch {branch} not tracked")

    job = await jobs.create(db, repo_id, branch)
    # send message to job queue
    with await request.app.redis_pool as conn:
        details = {
                "job_id": job["_id"],
                "repository_id": repo_id,
                "repository_url": repo["url"],
                "branch": branch,
                "targets": repo["targets"],
                "builds_dir": "/opt/haxebuilder/builds"}
        await conn.publish_json(app.pubsub_channel, details)

    return json(job)


def get_branch_from_request(request):
    try:
        return request.json["push"]["new"]["name"]
    except:
        return "master"


def validate_repo(repo):
    print(repo)
    return repo


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)

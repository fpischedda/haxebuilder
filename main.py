import asyncio
import aioredis
import motor.motor_asyncio
from sanic import Sanic
from sanic.response import json
from sanic.response import text
from sanic_cors import CORS
import uvloop
import auth
from builderlib import jobs
from builderlib import users
from builderlib import repositories
from utils import error_reason

asyncio.set_event_loop_policy(uvloop.EventLoopPolicy())

app = Sanic("haxebuilder")
app.config.from_envvar("HAXEBUILDER_CONFIG")
CORS(app, automatic_options=True)


def login_required(fun):

    async def wrapped(request, *args, **kwargs):
        request_user = get_user_from_request(request)

        if request_user is None:
            return error_reason("login required", status=401)
        return await fun(request, request_user, *args, **kwargs)

    return wrapped


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
        token = request.cookies.get("token", None)

    if token is None:
        return None
    return auth.user_from_token(
        token,
        request.app.config.SESSION_SECRET)


@app.route("/login", methods=["POST"])
async def login(request):
    db = request.app.db
    try:
        username = request.form.get("username", "")
        password = request.form.get("password", "")

        user = await auth.login(db, username, password)
        token = auth.generate_token(user, request.app.config.SESSION_SECRET)
        user["token"] = token
        response = json(user)
        response.cookies["token"] = token
        return response
    except auth.CredentialsError:
        return error_reason("wrong username or password")


@app.route("/users/new", methods=["POST"])
async def user_new(request):
    db = request.app.db
    try:
        user = await users.create(db, **request.json)
        return json(user)
    except users.UserExistsException:
        return error_reason("user already exists")


@app.route("/profile", methods=["GET", "OPTIONS"])
@login_required
async def user_profile(request, user):
    db = request.app.db
    user_id = user["_id"]
    user = await users.get_by_id(db, user_id)
    if user is None:
        return error_reason(f"user {user_id} not found")
    user["repositories"] = await repositories.get_all_by_user_id(db, user_id)
    return json(user)


@app.route("/repositories", methods=["GET", "OPTIONS"])
@login_required
async def user_repositories(request, user):
    print("repos")
    db = request.app.db
    user_id = user["_id"]
    repos = await repositories.get_all_by_user_id(db, user_id)
    return json(repos)


@app.route("/repositories", methods=["POST"])
@login_required
async def repository_new(request, user):
    db = request.app.db
    src_repo = validate_repo(request.json)
    repo = await repositories.create(db,
                                     user_id=user["_id"],
                                     **src_repo)
    if repo is None:
        return error_reason("unable to create new repository")
    return json(repo)


@app.route("/repositories/<repo_id>", methods=["GET", "OPTIONS"])
@login_required
async def repository_details(request, user, repo_id):
    db = request.app.db
    if not repositories.belongs_to_user(db, repo_id, user["_id"]):
        return error_reason(f"repository {repo_id} does not belong to you")
    repo = await repositories.get_by_id(db, repo_id)
    if repo is None:
        return error_reason(f"unable to find repository {repo_id}")
    return json(repo)


@app.route("/repositories/<repo_id>", methods=["DELETE"])
@login_required
async def repository_delete(request, user, repo_id):
    db = request.app.db
    if not await repositories.belongs_to_user(db, repo_id, user["_id"]):
        return error_reason(f"repository {repo_id} does not belong to you")
    await repositories.delete(db, repo_id)
    return json({"res": "OK", "message": f"repo {repo_id} correctly deleted"})


@app.route("/repositories", methods=["PUT", "PATCH"])
@login_required
async def repository_update(request, user, repo_id):
    db = request.app.db
    if not repositories.belongs_to_user(db, repo_id, user["_id"]):
        return error_reason(f"repository {repo_id} does not belong to you")
    src_repo = validate_repo(request.json)
    repo = await repositories.update(db,
                                     repo_id=repo_id,
                                     **src_repo)
    if repo is None:
        return error_reason(f"unable to update repo {repo_id}")
    return json(repo)


@app.route("/repositories/<repo_id>/jobs", methods=["GET", "OPTIONS"])
@login_required
async def repository_jobs(request, user, repo_id):
    db = request.app.db
    if not repositories.belongs_to_user(db, repo_id, user["_id"]):
        return error_reason(f"repository {repo_id} does not belong to you")
    job_list = await jobs.get_all_by_repository_id(db, repo_id)
    return json(job_list)


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
                "builds_dir": app.config.BUILDS_DIR}
        await conn.publish_json(app.pubsub_channel, details)

    return json(job)


def get_branch_from_request(request):
    try:
        return request.json["push"]["new"]["name"]
    except:
        return "master"


def validate_repo(repo):
    try:
        del repo["user_id"]
    except KeyError:
        pass
    try:
        del repo["repo_id"]
    except KeyError:
        pass
    return repo


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)

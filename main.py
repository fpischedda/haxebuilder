import asyncio
import uvloop


asyncio.set_event_loop_policy(uvloop.EventLoopPolicy())

import aioredis
import motor.motor_asyncio
from sanic import Sanic
from sanic.response import json
from builderlib import jobs
from builderlib import users
from builderlib import repositories
from utils import error_reason


app = Sanic()

@app.listener("before_server_start")
async def init_db(app, loop):
    client = motor.motor_asyncio.AsyncIOMotorClient()
    app.db = client["haxebuilder"]
    app.redis_pool = await aioredis.create_pool(
            ('localhost', 6379))


@app.route("/users/new", methods=["POST"])
async def test(request):
    db = request.app.db
    try:
        user = await users.create(db, **request.json)
        return json(user)
    except users.UserExistsException:
        return error_reason("user already exists")


@app.route("/users/<user_id>", methods=["GET"])
async def test(request, user_id):
    db = request.app.db
    user = await users.get_by_id(db, user_id)
    if user is None:
        return error_reason(f"user {user_id} not found")
    user["repositories"] = repositories.get_all_by_user_id(db, user_id) 
    return json(users)


@app.route("/repositories/new", methods=["POST"])
async def test(request):
    db = request.app.db
    src_repo = validate_repo(request.json)
    repo = await repositories.create(db, **src_repo)
    if repo is None:
        return error_reason("unable to create new repository")
    return json(repo)


@app.route("/repositories/<repo_id>", methods=["GET"])
async def test(request, repo_id):
    db = request.app.db
    repo = await repositories.get_by_id(db, repo_id)
    if repo is None:
        return error_reason(f"unable to find repository {repo_id}")
    return json(repo)


@app.route("/repositories/<repo_id>/build", methods=["POST"])
async def test(request, repo_id):
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
        await conn.publish_json('jobs', details)

    return json(job)


def get_branch_from_request(request):
    try:
        return request.json["push"]["changes"]["new"]["name"]
    except:
        return "master"


def validate_repo(repo):
    print(repo)
    return repo


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)

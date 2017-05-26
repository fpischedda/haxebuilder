"""
module for handling build job records
"""
from datetime import datetime
from . import auth


async def create(db, repo_id, branch):

    job = {
            "_id": auth.create_unique_id(),
            "repository_id": repo_id,
            "branch": branch,
            "status": "pending",
            "created_at": datetime.utcnow()
            }
    res = await db.jobs.insert_one(job)
    return job


async def get_by_id(db, job_id):
    return await db.repositories.find_one({"_id": job_id})


async def get_all_by_user_id(db, user_id):
    return await db.repositories.find(
            {"repository.user_id": user_id}).to_list()


async def get_all_by_repository_id(db, repository_id):
    return await db.repositories.find(
            {"repository._id": repository_id}).to_list()

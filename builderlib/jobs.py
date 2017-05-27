"""
module for handling build job records
"""
from datetime import datetime
from . import auth
from . import build


async def create(db, repo_id, repo_url, branch, target):

    job = {
            "_id": auth.create_unique_id(),
            "repository_id": repo_id,
            "repository_url": repo_url,
            "branch": branch,
            "target", target,
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


async def update(db, job_id, **changed):
    return await db.jobs.update_one({"_id": job_id}, {"set": changed})


async def run_build(db, job_id, repository_id,
        repository_url, branch, targets, builds_dir):

    await update_job(db, job_id, status="building")

    work_dir = f"/tmp/{job_id}"
    try:
        build.clone_repo(work_dir, repository_url, branch)
    except build.CloneError:
        await update_job(db, job_id, status="error")
        build.clean_work_dir(work_dir)
        return False

    target_errors = []
    for t in targets:
        try:
            build.build(work_dir, t)
            result_dir = os.path.join(build_dir, job_id, t)
            build.copy_build_result(work_dir, t, result_dir)
        except build.BuildError:
            target_error.append(t)

    if len(target_errors) > 0:
        await update_job(db, job_id, target_errors=target_errors)

    build.clean_work_dir(work_dir)
    await update_job(db, job_id, status="finished")
    return True

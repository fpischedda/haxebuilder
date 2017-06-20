"""
module for handling HaxeBuilder repositories
"""
from . import auth

async def create(db, user_id, name, url, tracked_branches, targets):
    repo = {
        "_id": auth.create_unique_id(),
        "user_id": user_id,
        "name": name,
        "url": url,
        "tracked_branches": tracked_branches,
        "targets": targets
    }
    res = await db.repositories.insert_one(repo)
    return repo


async def delete(db, repo_id):
    return await db.repositories.remove({"_id": repo_id})


async def update(db, repo_id, **properties):
    return await db.repositories.update({"_id": repo_id}, {"$set": properties})


async def get_by_id(db, repo_id):
    return await db.repositories.find_one({"_id": repo_id})


async def get_all_by_user_id(db, user_id):
    return await db.repositories.find({"user_id": user_id}).to_list(None)


async def belongs_to_user(db, repo_id, user_id):
    found = await db.repositories.find({
        "user_id": user_id,
        "_id": repo_id
    }).count()
    if found > 0:
        return True
    return False

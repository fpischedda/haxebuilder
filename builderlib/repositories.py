"""
module for handling HaxeBuilder repositories
"""
import uuid
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
    res =  await db.repositories.insert_one(repo)
    return repo


async def get_by_id(db, repo_id):
    return await db.repositories.find_one({"_id": repo_id})


async def get_all_by_user_id(db, user_id):
    return await db.repositories.find({"user_id": user_id}).to_list(None)

"""
module for handling user profiles
"""
import uuid
from . import auth


class UserExistsException(Exception):
    pass


async def user_exists(db, username, email):
    found = await db.profiles.find({
        "$or": [{"username": username}, {"email": email}]}).count()

    if found > 0:
        return True
    return False


async def create(db, username, email, password):
    exists = await user_exists(db, username, email)

    if exists:
        raise BuilderProfileExistsException

    password_hash = auth.get_hash(password)
    user = {
            "_id": auth.create_unique_id(),
            "username": username,
            "email": email,
            "password_hash": password_hash,
            }
    res = await db.profiles.insert_one(user)
    return user


async def get_by_id(db, user_id):
    return await db.profiles.find_one({"_id": user_id})

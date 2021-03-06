"""
module for handling user profiles
"""
from . import auth


class UserExistsException(Exception):
    pass


async def user_exists(db, username, email):
    found = await db.profiles.find({
        "$or": [{"username": username}, {"email": email}]}).count()

    return found > 0


async def create(db, username, email, password):
    exists = await user_exists(db, username, email)

    if exists:
        raise UserExistsException

    password_hash = auth.get_hash(password)
    user = {
            "_id": auth.create_unique_id(),
            "username": username,
            "email": email,
            "password_hash": password_hash,
            }
    await db.profiles.insert_one(user)
    return user


async def get_by_id(db, user_id):
    return await db.profiles.find_one({"_id": user_id})


async def get_by_username_password(db, username, password):
    return await db.profiles.find_one({
        "username": username,
        "password_hash": auth.get_hash(password)})

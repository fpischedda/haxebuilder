"""
module containing authenticantion helpers functions
"""
import jwt
from builderlib import users


class CredentialsError(Exception):
    pass


class InvalidSessionToken(Exception):
    pass


async def login(db, username, password):
    user = await users.get_by_username_password(db, username, password)
    if user is None:
        raise CredentialsError
    del user["password_hash"]
    return user


def generate_token(data, secret):
    return jwt.encode(data, secret, algorithm="HS256").decode("utf-8")


def validate_token(token, secret):
    try:
        return jwt.decode(token, secret)
    except:
        raise InvalidSessionToken


def user_from_token(token, secret):
    try:
        return jwt.decode(token, secret)
    except:
        return None

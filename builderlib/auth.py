import hashlib
import uuid


def create_unique_id():
    return str(uuid.uuid4())


def get_hash(text):
    return hashlib.sha224(text.encode("utf-8")).hexdigest()

from sanic.response import json


def error_reason(message, **kwargs):
    return json({"error": {"message": message}}, **kwargs)

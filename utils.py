from sanic.response import json


def error_reason(message):
    return json({"error": {"message": message}})

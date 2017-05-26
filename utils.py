from sanic.response import json


def error_reason(reason):
    return json({"res": "KO", "reason": reason})

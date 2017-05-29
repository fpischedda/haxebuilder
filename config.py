"""
configuration module, used both by sanic app and workers
"""
import os


MONGO_URL = "mongodb://localhost:27017"
MONGO_DB_NAME = "haxebuilder"
REDIS_HOST = "localhost"
REDIS_PORT = 6379
REDIS_PUBSUB_CHANNEL = "jobs"

SESSION_SECRET = "IZ5JUJOlnNeydN+N9MTsv9Kligkgt7a31/JtcTxJNd4="

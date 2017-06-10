"""
configuration module, used both by sanic app and workers
"""
import os

MONGO_URL = os.environ.get("MONGO_URL", "mongodb://localhost:27017")
MONGO_DB_NAME = os.environ.get("haxebuilder")
REDIS_HOST = os.environ.get("REDIS_HOST", "localhost")
REDIS_PORT = int(os.environ.get("REDIS_PORT", 6379))
REDIS_PUBSUB_CHANNEL = os.environ.get("PUB_SUB_CHANNEL", "jobs")

BUILDS_DIR = "/opt/haxebuilder/builds"

SESSION_SECRET = os.environ.get("SESSION_SECRET",
                                "IZ5JUJOlnNeydN+N9MTsv9Kligkgt7a31/JtcTxJNd4=")

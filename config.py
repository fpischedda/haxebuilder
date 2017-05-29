"""
configuration module, used both by sanic app and workers
"""
import os


MONGO_URL = "mongodb://localhost:27017"
REDIS_HOST = "localhost"
REDIS_PORT = 6379
REDIS_PUBSUB_CHANNEL = "jobs"

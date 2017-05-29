"""
the worker process that will handle the incoming build jobs
"""
import asyncio
import aioredis
import motor.motor_asyncio
from builderlib import jobs
import config


async def worker():
    print("Starting Jobs Worker...")
    client = motor.motor_asyncio.AsyncIOMotorClient(config.MONGO_URL)
    mongo_db = client["haxebuilder"]

    sub = await aioredis.create_redis(
                 (config.REDIS_HOST, config.REDIS_PORT))
    job_chan, *_ = await sub.subscribe(config.REDIS_PUBSUB_CHANNEL)

    print("Subscribed to jobs channel")
    print("Waiting for jobs")
    while await job_chan.wait_message():
        job = await job_chan.get_json(encoding='utf-8')
        print(f"received job: \n {job}")
        await jobs.run_build(mongo_db, **job)
        print(f"finished running job {job['job_id']}")


if __name__ == '__main__':

    asyncio.get_event_loop().run_until_complete(worker())

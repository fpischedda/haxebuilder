"""
thid module contains functions used to create a build
"""
import os.path
import shutil
import subprocess


def clone_repo(dest_dir, repo_url, branch):
    try:
        subprocess.run(
                ["git", "clone", "-b", branch, repo_url, dest_dir],
                check=True)
        return True
    except subprocess.CalledProcessError as e:
        return False


def build(work_dir, target):
    try:
        subprocess.run(
                ["lime", "build", target],
                cwd=work_dir,
                check=True)
        return True
    except subprocess.CalledProcessError as e:
        return False


def copy_build_result(work_dir, target, destination):
    origin = os.path.join(work_dir, "exports", target)
    shutil.copytree(origin, destination)


def clean_work_dir(work_dir):
    shutil.rmtree(work_dir, ignore_errors=True)

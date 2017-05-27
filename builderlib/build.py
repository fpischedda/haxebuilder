"""
thid module contains functions used to create a build
"""
import os.path
import shutil
import subprocess


class CloneError(Exception):
    pass


class BuildError(Exception):
    pass


def clone_repo(work_dir, repo_url, branch):
    try:
        subprocess.run(
                ["git", "clone", "-b", branch, repo_url, work_dir],
                check=True)
    except subprocess.CalledProcessError:
        raise CloneError


def build(work_dir, target):
    try:
        subprocess.run(
                ["lime", "build", target],
                cwd=work_dir,
                check=True)
        return True
    except subprocess.CalledProcessError:
        raise BuildError


def copy_build_result(work_dir, target, destination):
    origin = os.path.join(work_dir, "exports", target, "bin")
    shutil.copytree(origin, destination)


def clean_work_dir(work_dir):
    shutil.rmtree(work_dir, ignore_errors=True)

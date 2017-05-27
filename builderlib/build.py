"""
thid module contains functions used to create a build
"""
import shutil
import subprocess


def clone_repo(dest_dir, repo_url, branch):
    try:
        subprocess.run(
                ["git", "clone", "-b", branch, repo_url, dest_dir],
                check=True)
        return True
    except subprocess.CalledProcessError as e:
        print(e)
        return False


def build(work_dir, target):
    subprocess.run(["lime", "build", target], cwd=work_dir)


def compress_build(work_dir, target):
    pass


def clean_work_dir(work_dir):
    shutil.rmtree(work_dir, ignore_errors=True)

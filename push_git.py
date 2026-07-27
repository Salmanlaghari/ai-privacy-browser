import subprocess
import sys

def main():
    branch = "jules-7872924799163510562-9c134fc3"
    print(f"Pushing branch {branch} programmatically...")
    result = subprocess.run(["git", "push", "origin", branch], capture_output=True, text=True)
    print("STDOUT:", result.stdout)
    print("STDERR:", result.stderr)
    if result.returncode == 0:
        print("Push successful!")
    else:
        print("Push failed with code:", result.returncode)
        sys.exit(result.returncode)

if __name__ == "__main__":
    main()

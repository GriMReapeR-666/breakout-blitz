import os
import subprocess
import sys

# Forward compatibility wrapper
sys.stdout.reconfigure(encoding='utf-8')
print("Running verified trading lobby and mastery suite...")
res = subprocess.run([sys.executable, "scripts/verify_trading_lobby_and_mastery.py"])
sys.exit(res.returncode)

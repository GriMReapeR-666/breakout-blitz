import os
import glob

assets_dir = r"app/src/main/assets/setuprush/assets"
js_files = glob.glob(os.path.join(assets_dir, "*.js"))

print("Unique modules:")
modules = set()
for f in js_files:
    base = os.path.basename(f)
    # strip hash and extension
    prefix = base.split("-")[0]
    modules.add(prefix)

print(sorted(list(modules)))

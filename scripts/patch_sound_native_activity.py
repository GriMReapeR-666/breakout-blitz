import sys

sys.stdout.reconfigure(encoding='utf-8')
p = 'app/src/main/java/com/setuprush/candy/NativeGameActivity.kt'
with open(p, 'r', encoding='utf-8') as f:
    text = f.read()

target = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bridge = NativeBridge(this)"""

replacement = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bridge = NativeBridge(this)
        com.setuprush.candy.engine.SoundEffectManager.initialize(this)"""

text = text.replace(target.replace('\r\n', '\n'), replacement.replace('\r\n', '\n'))
text = text.replace(target, replacement)

target_destroy = """    override fun onDestroy() {
        super.onDestroy()
    }"""
# check if onDestroy exists
if "override fun onDestroy" not in text:
    target_end = """            NativeGameScreen(
                bridge = bridge
            )
        }
    }
}"""
    replacement_end = """            NativeGameScreen(
                bridge = bridge
            )
        }
    }

    override fun onDestroy() {
        com.setuprush.candy.engine.SoundEffectManager.release()
        super.onDestroy()
    }
}"""
    text = text.replace(target_end.replace('\r\n', '\n'), replacement_end.replace('\r\n', '\n'))
    text = text.replace(target_end, replacement_end)

with open(p, 'w', encoding='utf-8') as f:
    f.write(text)

print('Updated NativeGameActivity.kt with SoundEffectManager lifecycle')

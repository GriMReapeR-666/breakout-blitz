import sys

sys.stdout.reconfigure(encoding='utf-8')
p = 'app/src/main/java/com/setuprush/candy/NativeGameActivity.kt'
with open(p, 'r', encoding='utf-8') as f:
    text = f.read()

# 1. Update NativeGameScreen call in onCreate
old_call = """            NativeGameScreen(
                bridge = bridge,
                onSwitchToWebView = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            )"""

new_call = """            NativeGameScreen(
                bridge = bridge
            )"""

text = text.replace(old_call.replace('\r\n', '\n'), new_call.replace('\r\n', '\n'))
text = text.replace(old_call, new_call)

# 2. Update NativeGameScreen signature
old_sig = """@Composable
fun NativeGameScreen(
    bridge: NativeBridge,
    onSwitchToWebView: () -> Unit
)"""

new_sig = """@Composable
fun NativeGameScreen(
    bridge: NativeBridge,
    onSwitchToWebView: (() -> Unit)? = null
)"""

text = text.replace(old_sig.replace('\r\n', '\n'), new_sig.replace('\r\n', '\n'))
text = text.replace(old_sig, new_sig)

# 3. Replace WEB button with STATS button
old_btn = """                    Button(
                        onClick = onSwitchToWebView,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.bull),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text("WEB", color = if (theme.isLight) Color.White else Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }"""

new_btn = """                    Button(
                        onClick = { showPostMatchDebriefDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.bull.copy(alpha = 0.18f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text("📊 STATS", color = theme.bull, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }"""

text = text.replace(old_btn.replace('\r\n', '\n'), new_btn.replace('\r\n', '\n'))
text = text.replace(old_btn, new_btn)

with open(p, 'w', encoding='utf-8') as f:
    f.write(text)

print('Updated NativeGameActivity.kt successfully')

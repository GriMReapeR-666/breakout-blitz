vi=[{id:`supertrend`,name:`Supertrend`,short:`ST`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("Supertrend", overlay=true)
atrLen = input.int(10, "ATR length")
mult = input.float(3.0, "Multiplier")
[st, dir] = ta.supertrend(mult, atrLen)
body = plot((open + close) / 2, "Body", display=display.none)
upL = plot(dir < 0 ? st : na, "Up", color=#8AB4F8, linewidth=2, style=plot.style_linebr)
dnL = plot(dir > 0 ? st : na, "Down", color=#B4A2FF, linewidth=2, style=plot.style_linebr)
fill(body, upL, color=color.new(#8AB4F8, 94), title="Up zone")
fill(body, dnL, color=color.new(#B4A2FF, 93), title="Down zone")
plotshape(dir < 0 and dir[1] > 0 ? st : na, "Buy", shape.labelup, location.absolute, #F4F3EF, text="BUY", textcolor=#0A0A0B, size=size.tiny)
plotshape(dir > 0 and dir[1] < 0 ? st : na, "Sell", shape.labeldown, location.absolute, #FF4B3E, text="SELL", textcolor=#0A0A0B, size=size.tiny)
`},{id:`structure`,name:`Market Structure`,short:`BOS`,pane:!1,tag:`Smart money`,src:`//@version=6
indicator("Market Structure", overlay=true, max_lines_count=60, max_labels_count=60)
len = input.int(5, "Swing length")
ph = ta.pivothigh(high, len, len)
pl = ta.pivotlow(low, len, len)
var float hiLvl = na
var int hiIdx = na
var bool hiLive = false
var float loLvl = na
var int loIdx = na
var bool loLive = false
var int trend = 0
if not na(ph)
    hiLvl := ph
    hiIdx := bar_index - len
    hiLive := true
if not na(pl)
    loLvl := pl
    loIdx := bar_index - len
    loLive := true
if hiLive and close > hiLvl
    choch = trend < 0
    line.new(hiIdx, hiLvl, bar_index, hiLvl, color=#F4F3EF, width=1, style=choch ? line.style_dashed : line.style_solid)
    label.new(math.round((hiIdx + bar_index) / 2), hiLvl, choch ? "CHoCH" : "BOS", style=label.style_label_down, color=color.new(#0A0A0B, 100), textcolor=#F4F3EF, size=size.tiny)
    hiLive := false
    trend := 1
if loLive and close < loLvl
    choch = trend > 0
    line.new(loIdx, loLvl, bar_index, loLvl, color=#FF4B3E, width=1, style=choch ? line.style_dashed : line.style_solid)
    label.new(math.round((loIdx + bar_index) / 2), loLvl, choch ? "CHoCH" : "BOS", style=label.style_label_up, color=color.new(#0A0A0B, 100), textcolor=#FF4B3E, size=size.tiny)
    loLive := false
    trend := -1
plotshape(ph, "Swing high", shape.triangledown, location.absolute, color.new(#8A8A8E, 30), offset=-len, size=size.tiny)
plotshape(pl, "Swing low", shape.triangleup, location.absolute, color.new(#8A8A8E, 30), offset=-len, size=size.tiny)
`},{id:`fvg`,name:`Fair Value Gaps`,short:`FVG`,pane:!1,tag:`Smart money`,src:`//@version=6
indicator("Fair Value Gaps", overlay=true, max_boxes_count=60)
minPct = input.float(0.05, "Min gap %")
var bulls = array.new<box>()
var bears = array.new<box>()
bullGap = low > high[2] and close[1] > high[2] and (low - high[2]) / close * 100 >= minPct
bearGap = high < low[2] and close[1] < low[2] and (low[2] - high) / close * 100 >= minPct
if bullGap
    array.push(bulls, box.new(bar_index - 2, low, bar_index, high[2], border_color=color.new(#F4F3EF, 60), bgcolor=color.new(#F4F3EF, 93), text="FVG", text_color=color.new(#F4F3EF, 40), text_size=size.tiny, text_halign=text.align_left))
if bearGap
    array.push(bears, box.new(bar_index - 2, low[2], bar_index, high, border_color=color.new(#FF4B3E, 60), bgcolor=color.new(#FF4B3E, 93), text="FVG", text_color=color.new(#FF4B3E, 40), text_size=size.tiny, text_halign=text.align_left))
if array.size(bulls) > 0
    for i = array.size(bulls) - 1 to 0
        b = array.get(bulls, i)
        box.set_right(b, bar_index)
        if low < box.get_bottom(b)
            box.delete(b)
            array.remove(bulls, i)
if array.size(bears) > 0
    for i = array.size(bears) - 1 to 0
        b = array.get(bears, i)
        box.set_right(b, bar_index)
        if high > box.get_top(b)
            box.delete(b)
            array.remove(bears, i)
while array.size(bulls) > 3
    box.delete(array.shift(bulls))
while array.size(bears) > 3
    box.delete(array.shift(bears))
`},{id:`orderblocks`,name:`Order Blocks`,short:`OB`,pane:!1,tag:`Smart money`,src:`//@version=6
indicator("Order Blocks", overlay=true, max_boxes_count=40)
len = input.int(5, "Swing length")
ph = ta.pivothigh(high, len, len)
pl = ta.pivotlow(low, len, len)
var float hiLvl = na
var float loLvl = na
var int lastDn = na
var int lastUp = na
var obs = array.new<box>()
var dirs = array.new<int>()
if close < open
    lastDn := bar_index
if close > open
    lastUp := bar_index
if not na(ph)
    hiLvl := ph
if not na(pl)
    loLvl := pl
if not na(hiLvl) and close > hiLvl and not na(lastDn)
    d = bar_index - lastDn
    array.push(obs, box.new(lastDn, high[d], bar_index, low[d], border_color=color.new(#F4F3EF, 70), bgcolor=color.new(#F4F3EF, 93), text="OB", text_color=color.new(#F4F3EF, 30), text_size=size.tiny, text_halign=text.align_right))
    array.push(dirs, 1)
    hiLvl := na
if not na(loLvl) and close < loLvl and not na(lastUp)
    u = bar_index - lastUp
    array.push(obs, box.new(lastUp, high[u], bar_index, low[u], border_color=color.new(#FF4B3E, 70), bgcolor=color.new(#FF4B3E, 93), text="OB", text_color=color.new(#FF4B3E, 30), text_size=size.tiny, text_halign=text.align_right))
    array.push(dirs, -1)
    loLvl := na
if array.size(obs) > 0
    for i = array.size(obs) - 1 to 0
        b = array.get(obs, i)
        dir = array.get(dirs, i)
        box.set_right(b, bar_index)
        broken = dir > 0 ? close < box.get_bottom(b) : close > box.get_top(b)
        if broken and box.get_left(b) < bar_index - 3
            box.delete(b)
            array.remove(obs, i)
            array.remove(dirs, i)
while array.size(obs) > 3
    box.delete(array.shift(obs))
    array.shift(dirs)
`},{id:`volprofile`,name:`Volume Profile`,short:`VP`,pane:!1,tag:`Volume`,src:`//@version=6
indicator("Volume Profile", overlay=true, max_boxes_count=60, max_lines_count=4)
look = input.int(120, "Bars")
rows = input.int(24, "Rows")
width = input.int(7, "Width (bars)")
if barstate.islast
    n = math.min(look, bar_index + 1)
    hi = ta.highest(high, n)
    lo = ta.lowest(low, n)
    step = (hi - lo) / rows
    ups = array.new<float>(rows, 0.0)
    dns = array.new<float>(rows, 0.0)
    for i = 0 to n - 1
        mid = (high[i] + low[i]) / 2
        r = math.min(rows - 1, math.max(0, int(math.floor((mid - lo) / step))))
        if close[i] >= open[i]
            array.set(ups, r, array.get(ups, r) + volume[i])
        else
            array.set(dns, r, array.get(dns, r) + volume[i])
    tot = array.new<float>(rows, 0.0)
    for r = 0 to rows - 1
        array.set(tot, r, array.get(ups, r) + array.get(dns, r))
    mx = array.max(tot)
    poc = array.indexof(tot, mx)
    for r = 0 to rows - 1
        t = array.get(tot, r)
        w = mx > 0 ? t / mx * width : 0
        wu = t > 0 ? w * array.get(ups, r) / t : 0
        y1 = lo + step * (r + 1)
        y0 = lo + step * r
        x0 = bar_index + 1
        box.new(x0, y1, x0 + wu, y0, border_color=na, bgcolor=color.new(#F4F3EF, r == poc ? 40 : 75))
        box.new(x0 + wu, y1, x0 + w, y0, border_color=na, bgcolor=color.new(#FF4B3E, r == poc ? 40 : 75))
    pocY = lo + step * (poc + 0.5)
    line.new(bar_index - n + 1, pocY, bar_index + 1 + width, pocY, color=#E9B949, width=1, style=line.style_dotted)
`},{id:`levels`,name:`Support & Resistance`,short:`S/R`,pane:!1,tag:`Levels`,src:`//@version=6
indicator("Support & Resistance", overlay=true, max_lines_count=20, max_labels_count=20)
len = input.int(8, "Pivot length")
ph = ta.pivothigh(high, len, len)
pl = ta.pivotlow(low, len, len)
var res = array.new<line>()
var sup = array.new<line>()
var brk = array.new<label>()
if not na(ph)
    array.push(res, line.new(bar_index - len, ph, bar_index, ph, color=color.new(#FF4B3E, 20), width=2, extend=extend.right))
if not na(pl)
    array.push(sup, line.new(bar_index - len, pl, bar_index, pl, color=color.new(#F4F3EF, 20), width=2, extend=extend.right))
if array.size(res) > 0
    for i = array.size(res) - 1 to 0
        l = array.get(res, i)
        if close > line.get_y1(l) and close[1] <= line.get_y1(l)
            line.set_extend(l, extend.none)
            line.set_x2(l, bar_index)
            line.set_color(l, color.new(#FF4B3E, 70))
            array.push(brk, label.new(bar_index, line.get_y1(l), "BREAK", style=label.style_label_up, color=#F4F3EF, textcolor=#0A0A0B, size=size.tiny))
            array.remove(res, i)
if array.size(sup) > 0
    for i = array.size(sup) - 1 to 0
        l = array.get(sup, i)
        if close < line.get_y1(l) and close[1] >= line.get_y1(l)
            line.set_extend(l, extend.none)
            line.set_x2(l, bar_index)
            line.set_color(l, color.new(#F4F3EF, 70))
            array.push(brk, label.new(bar_index, line.get_y1(l), "BREAK", style=label.style_label_down, color=#FF4B3E, textcolor=#0A0A0B, size=size.tiny))
            array.remove(sup, i)
while array.size(res) > 3
    line.set_extend(array.shift(res), extend.none)
while array.size(sup) > 3
    line.set_extend(array.shift(sup), extend.none)
while array.size(brk) > 2
    label.delete(array.shift(brk))
`},{id:`bbands`,name:`Bollinger Bands`,short:`BB`,pane:!1,tag:`Volatility`,src:`//@version=6
indicator("Bollinger Bands", overlay=true)
len = input.int(20, "Length")
mult = input.float(2.0, "StdDev")
[basis, upper, lower] = ta.bb(close, len, mult)
bw = (upper - lower) / basis
squeeze = bw <= ta.lowest(bw, 60)
plot(basis, "Basis", color=#E9B949, linewidth=1)
u = plot(upper, "Upper", color=color.new(#8AB4F8, 20))
l = plot(lower, "Lower", color=color.new(#8AB4F8, 20))
fill(u, l, color=color.new(#8AB4F8, 92), title="Band")
bgcolor(squeeze ? color.new(#E9B949, 95) : na, title="Squeeze")
`},{id:`ichimoku`,name:`Ichimoku Cloud`,short:`ICH`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("Ichimoku Cloud", overlay=true)
conv = input.int(9, "Conversion")
base = input.int(26, "Base")
spanB = input.int(52, "Span B")
disp = input.int(26, "Displacement")
donchian(len) => math.avg(ta.lowest(len), ta.highest(len))
c = donchian(conv)
b = donchian(base)
a1 = math.avg(c, b)
b1 = donchian(spanB)
plot(c, "Conversion", color=#8AB4F8)
plot(b, "Base", color=#E9B949)
plot(close, "Lagging", offset=-disp + 1, color=color.new(#8A8A8E, 30))
p1 = plot(a1, "Lead A", offset=disp - 1, color=color.new(#8AB4F8, 50))
p2 = plot(b1, "Lead B", offset=disp - 1, color=color.new(#B4A2FF, 50))
fill(p1, p2, color=a1 > b1 ? color.new(#8AB4F8, 88) : color.new(#B4A2FF, 88), title="Cloud")
`},{id:`vwap`,name:`VWAP`,short:`VWAP`,pane:!1,tag:`Volume`,src:`//@version=6
indicator("VWAP", overlay=true)
mult = input.float(1.0, "Band")
[v, up, dn] = ta.vwap(hlc3, timeframe.change("D") or barstate.isfirst, mult)
plot(v, "VWAP", color=#E9B949, linewidth=2)
u = plot(up, "Upper", color=color.new(#E9B949, 60))
d = plot(dn, "Lower", color=color.new(#E9B949, 60))
fill(u, d, color=color.new(#E9B949, 93), title="Band")
`},{id:`ribbon`,name:`EMA Ribbon`,short:`EMA`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("EMA Ribbon", overlay=true)
e1 = ta.ema(close, 8)
e2 = ta.ema(close, 13)
e3 = ta.ema(close, 21)
e4 = ta.ema(close, 34)
e5 = ta.ema(close, 55)
up = e1 > e5
c1 = up ? #8AB4F8 : #B4A2FF
p1 = plot(e1, "EMA 8", color=c1, linewidth=2)
plot(e2, "EMA 13", color=color.new(c1, 30))
plot(e3, "EMA 21", color=color.new(c1, 50))
plot(e4, "EMA 34", color=color.new(c1, 65))
p5 = plot(e5, "EMA 55", color=color.new(c1, 75), linewidth=2)
fill(p1, p5, color=color.new(c1, 92), title="Ribbon")
`},{id:`heikin`,name:`Heikin Ashi`,short:`HA`,pane:!1,tag:`Look`,src:`//@version=6
indicator("Heikin Ashi", overlay=true)
haC = (open + high + low + close) / 4
var float haO = na
haO := na(haO[1]) ? (open + close) / 2 : (haO[1] + haC[1]) / 2
haH = math.max(high, math.max(haO, haC))
haL = math.min(low, math.min(haO, haC))
barcolor(color.new(#000000, 100), title="Hide bars")
plotcandle(haO, haH, haL, haC, "HA", color=haC >= haO ? color.new(#0A0A0B, 0) : #FF4B3E, wickcolor=haC >= haO ? #F4F3EF : #FF4B3E, bordercolor=haC >= haO ? #F4F3EF : #FF4B3E)
`},{id:`rsi`,name:`RSI`,short:`RSI`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("RSI", overlay=false)
len = input.int(14, "Length")
r = ta.rsi(close, len)
rp = plot(r, "RSI", color=r > 70 ? #F4F3EF : r < 30 ? #FF4B3E : #E9B949, linewidth=2)
top = plot(70, "70", color=color.new(#8A8A8E, 60), display=display.none)
bot = plot(30, "30", color=color.new(#8A8A8E, 60), display=display.none)
hline(70, "Overbought", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
hline(50, "Mid", color=color.new(#8A8A8E, 70), linestyle=hline.style_dotted)
hline(30, "Oversold", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
fill(top, bot, color=color.new(#8A8A8E, 94), title="Band")
fill(rp, top, color=r > 70 ? color.new(#F4F3EF, 70) : na, title="OB")
fill(rp, bot, color=r < 30 ? color.new(#FF4B3E, 70) : na, title="OS")
`},{id:`macd`,name:`MACD`,short:`MACD`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("MACD", overlay=false)
fast = input.int(12, "Fast")
slow = input.int(26, "Slow")
sig = input.int(9, "Signal")
[m, s, h] = ta.macd(close, fast, slow, sig)
col = h >= 0 ? (h > h[1] ? #F4F3EF : color.new(#F4F3EF, 55)) : (h < h[1] ? #FF4B3E : color.new(#FF4B3E, 55))
plot(h, "Histogram", color=col, style=plot.style_columns)
plot(m, "MACD", color=#E9B949, linewidth=2)
plot(s, "Signal", color=#8AB4F8)
hline(0, "Zero", color=color.new(#8A8A8E, 60))
`},{id:`squeeze`,name:`Squeeze Momentum`,short:`SQZ`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Squeeze Momentum", overlay=false)
len = input.int(20, "Length")
[bbM, bbU, bbL] = ta.bb(close, len, 2.0)
kcRange = ta.sma(ta.tr(true), len) * 1.5
kcU = bbM + kcRange
kcL = bbM - kcRange
sqzOn = bbL > kcL and bbU < kcU
mid = math.avg(math.avg(ta.highest(high, len), ta.lowest(low, len)), ta.sma(close, len))
val = ta.linreg(close - mid, len, 0)
col = val >= 0 ? (val > nz(val[1]) ? #F4F3EF : color.new(#F4F3EF, 55)) : (val < nz(val[1]) ? #FF4B3E : color.new(#FF4B3E, 55))
plot(val, "Momentum", color=col, style=plot.style_columns)
plot(0, "Squeeze", color=sqzOn ? #E9B949 : color.new(#8A8A8E, 50), style=plot.style_circles, linewidth=3)
`},{id:`wavetrend`,name:`WaveTrend`,short:`WT`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("WaveTrend", overlay=false)
n1 = input.int(10, "Channel")
n2 = input.int(21, "Average")
ap = hlc3
esa = ta.ema(ap, n1)
d = ta.ema(math.abs(ap - esa), n1)
ci = (ap - esa) / (0.015 * d)
wt1 = ta.ema(ci, n2)
wt2 = ta.sma(wt1, 4)
plot(wt1 - wt2, "Spread", color=color.new(#8AB4F8, 60), style=plot.style_area)
plot(wt1, "WT1", color=#F4F3EF, linewidth=2)
plot(wt2, "WT2", color=#FF4B3E)
hline(60, "OB", color=color.new(#8A8A8E, 50), linestyle=hline.style_dashed)
hline(-60, "OS", color=color.new(#8A8A8E, 50), linestyle=hline.style_dashed)
hline(0, "Zero", color=color.new(#8A8A8E, 75))
plotshape(ta.crossover(wt1, wt2) ? wt1 : na, "Up cross", shape.circle, location.absolute, #F4F3EF, size=size.tiny)
plotshape(ta.crossunder(wt1, wt2) ? wt1 : na, "Down cross", shape.circle, location.absolute, #FF4B3E, size=size.tiny)
`},{id:`emacross`,name:`EMA 50/200 Cross`,short:`GX`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("EMA 50/200 Cross", overlay=true)
fast = ta.ema(close, input.int(50, "Fast"))
slow = ta.ema(close, input.int(200, "Slow"))
f = plot(fast, "EMA 50", color=#8AB4F8, linewidth=2)
sl = plot(slow, "EMA 200", color=#B4A2FF, linewidth=2)
fill(f, sl, color=fast > slow ? color.new(#8AB4F8, 92) : color.new(#B4A2FF, 92), title="Spread")
plotshape(ta.crossover(fast, slow) ? slow : na, "Golden", shape.labelup, location.absolute, #F4F3EF, text="GOLDEN", textcolor=#0A0A0B, size=size.tiny)
plotshape(ta.crossunder(fast, slow) ? slow : na, "Death", shape.labeldown, location.absolute, #FF4B3E, text="DEATH", textcolor=#0A0A0B, size=size.tiny)
`},{id:`hull`,name:`Hull MA`,short:`HMA`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("Hull MA", overlay=true)
len = input.int(55, "Length")
h = ta.hma(close, len)
plot(h, "HMA", color=h > h[2] ? #8AB4F8 : #B4A2FF, linewidth=2)
`},{id:`psar`,name:`Parabolic SAR`,short:`SAR`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("Parabolic SAR", overlay=true)
s = ta.sar(input.float(0.02, "Start"), input.float(0.02, "Step"), input.float(0.2, "Max"))
plot(s, "SAR", color=s < close ? #8AB4F8 : #B4A2FF, style=plot.style_circles, linewidth=2)
`},{id:`keltner`,name:`Keltner Channels`,short:`KC`,pane:!1,tag:`Volatility`,src:`//@version=6
indicator("Keltner Channels", overlay=true)
len = input.int(20, "Length")
mult = input.float(2.0, "ATR multiplier")
[m, u, l] = ta.kc(close, len, mult)
up = plot(u, "Upper", color=color.new(#8AB4F8, 20))
lo = plot(l, "Lower", color=color.new(#8AB4F8, 20))
plot(m, "Basis", color=#8AB4F8, linewidth=2)
fill(up, lo, color=color.new(#8AB4F8, 94), title="Channel")
`},{id:`donchian`,name:`Donchian Channels`,short:`DC`,pane:!1,tag:`Volatility`,src:`//@version=6
indicator("Donchian Channels", overlay=true)
len = input.int(20, "Length")
u = ta.highest(high, len)
l = ta.lowest(low, len)
up = plot(u, "Upper", color=#8AB4F8, style=plot.style_stepline)
lo = plot(l, "Lower", color=#B4A2FF, style=plot.style_stepline)
plot(math.avg(u, l), "Mid", color=color.new(#8A8A8E, 30), style=plot.style_stepline)
fill(up, lo, color=color.new(#8A8A8E, 95), title="Range")
plotshape(close > u[1] ? low : na, "Breakout up", shape.triangleup, location.belowbar, #F4F3EF, size=size.tiny)
plotshape(close < l[1] ? high : na, "Breakout down", shape.triangledown, location.abovebar, #FF4B3E, size=size.tiny)
`},{id:`chandelier`,name:`Chandelier Exit`,short:`CE`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("Chandelier Exit", overlay=true)
len = input.int(22, "Length")
mult = input.float(3.0, "ATR multiplier")
atr = mult * ta.atr(len)
longStop = ta.highest(close, len) - atr
longStopPrev = nz(longStop[1], longStop)
longStop := close[1] > longStopPrev ? math.max(longStop, longStopPrev) : longStop
shortStop = ta.lowest(close, len) + atr
shortStopPrev = nz(shortStop[1], shortStop)
shortStop := close[1] < shortStopPrev ? math.min(shortStop, shortStopPrev) : shortStop
var int dir = 1
dir := close > shortStopPrev ? 1 : close < longStopPrev ? -1 : dir
plot(dir == 1 ? longStop : na, "Long stop", color=#8AB4F8, linewidth=2, style=plot.style_linebr)
plot(dir == -1 ? shortStop : na, "Short stop", color=#B4A2FF, linewidth=2, style=plot.style_linebr)
plotshape(dir == 1 and dir[1] == -1 ? longStop : na, "Flip up", shape.labelup, location.absolute, #F4F3EF, text="LONG", textcolor=#0A0A0B, size=size.tiny)
plotshape(dir == -1 and dir[1] == 1 ? shortStop : na, "Flip down", shape.labeldown, location.absolute, #FF4B3E, text="SHORT", textcolor=#0A0A0B, size=size.tiny)
`},{id:`alligator`,name:`Williams Alligator`,short:`ALLI`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("Williams Alligator", overlay=true)
jaw = ta.rma(hl2, input.int(13, "Jaw"))
teeth = ta.rma(hl2, input.int(8, "Teeth"))
lips = ta.rma(hl2, input.int(5, "Lips"))
plot(jaw, "Jaw", color=#8AB4F8, offset=8)
plot(teeth, "Teeth", color=#B4A2FF, offset=5)
plot(lips, "Lips", color=#8AB4F8, offset=3)
`},{id:`tema`,name:`TEMA / DEMA`,short:`TEMA`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("TEMA / DEMA", overlay=true)
len = input.int(21, "Length")
e1 = ta.ema(close, len)
e2 = ta.ema(e1, len)
e3 = ta.ema(e2, len)
dema = 2 * e1 - e2
tema = 3 * e1 - 3 * e2 + e3
plot(tema, "TEMA", color=#8AB4F8, linewidth=2)
plot(dema, "DEMA", color=#E9B949)
`},{id:`linreg`,name:`LinReg Channel`,short:`LRC`,pane:!1,tag:`Levels`,src:`//@version=6
indicator("Linear Regression Channel", overlay=true, max_lines_count=3)
len = input.int(100, "Length")
mult = input.float(2.0, "Deviation")
e = ta.linreg(close, len, 0)
s = ta.linreg(close, len, len - 1)
if barstate.islast and not na(e)
    slope = (e - s) / (len - 1)
    ss = 0.0
    for i = 0 to len - 1
        r = close[i] - (e - slope * i)
        ss += r * r
    dev = math.sqrt(ss / (len - 1)) // TradingView's channel: residual deviation over len - 1
    u = line.new(bar_index - len + 1, s + mult * dev, bar_index, e + mult * dev, color=color.new(#B4A2FF, 20))
    l = line.new(bar_index - len + 1, s - mult * dev, bar_index, e - mult * dev, color=color.new(#B4A2FF, 20))
    linefill.new(u, l, color.new(#B4A2FF, 94))
    line.new(bar_index - len + 1, s, bar_index, e, color=#B4A2FF, width=2)
`},{id:`envelope`,name:`MA Envelope`,short:`ENV`,pane:!1,tag:`Volatility`,src:`//@version=6
indicator("MA Envelope", overlay=true)
len = input.int(20, "Length")
pct = input.float(1.5, "Percent")
m = ta.sma(close, len)
up = plot(m * (1 + pct / 100), "Upper", color=color.new(#8A8A8E, 20))
lo = plot(m * (1 - pct / 100), "Lower", color=color.new(#8A8A8E, 20))
plot(m, "Basis", color=#8A8A8E, linewidth=2)
fill(up, lo, color=color.new(#8A8A8E, 95), title="Envelope")
`},{id:`pivots`,name:`Pivot Points`,short:`PP`,pane:!1,tag:`Levels`,src:`//@version=6
indicator("Pivot Points", overlay=true)
// Blocks are 4 hours on intraday charts; on 4h and daily charts each bar would start its own block, so they step up to days and weeks.
var int step = na
if bar_index == 1
    step := time - time[1]
big = not na(step) and step >= 4 * 3600000
huge = not na(step) and step >= 86400000
nb = huge ? timeframe.change("W") : big ? timeframe.change("D") : timeframe.change("240")
var float bh = na
var float bl = na
var float pH = na
var float pL = na
var float pC = na
var int blocks = 0
if nb
    blocks += 1
    // The chart's first block is usually partial: it never seeds the pivots.
    if blocks > 1
        pH := bh
        pL := bl
        pC := close[1]
    bh := high
    bl := low
else
    bh := na(bh) ? high : math.max(bh, high)
    bl := na(bl) ? low : math.min(bl, low)
pp = (pH + pL + pC) / 3
plot(nb ? na : pp, "P", color=#8A8A8E, linewidth=2, style=plot.style_linebr)
plot(nb ? na : 2 * pp - pL, "R1", color=color.new(#8AB4F8, 30), style=plot.style_linebr)
plot(nb ? na : pp + (pH - pL), "R2", color=color.new(#8AB4F8, 50), style=plot.style_linebr)
plot(nb ? na : 2 * pp - pH, "S1", color=color.new(#B4A2FF, 30), style=plot.style_linebr)
plot(nb ? na : pp - (pH - pL), "S2", color=color.new(#B4A2FF, 50), style=plot.style_linebr)
`},{id:`fib`,name:`Auto Fibonacci`,short:`FIB`,pane:!1,tag:`Levels`,src:`//@version=6
indicator("Auto Fibonacci", overlay=true, max_lines_count=10, max_labels_count=10)
len = input.int(80, "Swing window")
h = ta.highest(high, len)
l = ta.lowest(low, len)
hb = ta.highestbars(high, len)
lb = ta.lowestbars(low, len)
up = lb < hb
r = h - l
if barstate.islast and not na(r)
    x0 = bar_index + math.min(hb, lb)
    for lv in array.from(0.0, 0.382, 0.5, 0.618, 1.0)
        y = up ? h - r * lv : l + r * lv
        line.new(x0, y, bar_index, y, color=lv == 0.618 ? #E9B949 : color.new(#8A8A8E, 30), width=1)
        label.new(bar_index, y, str.tostring(lv * 100, "#.#"), style=label.style_label_left, color=color.new(#0A0A0B, 100), textcolor=#8A8A8E, size=size.tiny)
`},{id:`zigzag`,name:`ZigZag`,short:`ZZ`,pane:!1,tag:`Levels`,src:`//@version=6
indicator("ZigZag", overlay=true, max_lines_count=100)
len = input.int(6, "Pivot length")
ph = ta.pivothigh(high, len, len)
pl = ta.pivotlow(low, len, len)
var float lastP = na
var int lastI = na
var int lastDir = 0
if not na(ph) and lastDir != 1
    if not na(lastP)
        line.new(lastI, lastP, bar_index - len, ph, color=#F4F3EF, width=1)
    lastP := ph
    lastI := bar_index - len
    lastDir := 1
if not na(pl) and lastDir != -1
    if not na(lastP)
        line.new(lastI, lastP, bar_index - len, pl, color=#FF4B3E, width=1)
    lastP := pl
    lastI := bar_index - len
    lastDir := -1
`},{id:`stoch`,name:`Stochastic`,short:`STO`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Stochastic", overlay=false)
k = ta.sma(ta.stoch(close, high, low, input.int(14, "K")), 3)
d = ta.sma(k, 3)
plot(k, "%K", color=#F4F3EF, linewidth=2)
plot(d, "%D", color=#FF4B3E)
hline(80, "OB", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
hline(20, "OS", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
plotshape(ta.crossover(k, d) and k < 25 ? k : na, "Buy", shape.circle, location.absolute, #F4F3EF, size=size.tiny)
plotshape(ta.crossunder(k, d) and k > 75 ? k : na, "Sell", shape.circle, location.absolute, #FF4B3E, size=size.tiny)
`},{id:`stochrsi`,name:`Stochastic RSI`,short:`SRSI`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Stochastic RSI", overlay=false)
len = input.int(14, "Length")
r = ta.rsi(close, len)
k = ta.sma(ta.stoch(r, r, r, len), 3)
d = ta.sma(k, 3)
plot(k, "%K", color=#F4F3EF, linewidth=2)
plot(d, "%D", color=#FF4B3E)
hline(80, "OB", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
hline(20, "OS", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
`},{id:`cci`,name:`CCI`,short:`CCI`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("CCI", overlay=false)
c = ta.cci(hlc3, input.int(20, "Length"))
plot(c, "CCI", color=c > 100 ? #F4F3EF : c < -100 ? #FF4B3E : #E9B949, linewidth=2)
hline(100, "100", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
hline(0, "0", color=color.new(#8A8A8E, 70), linestyle=hline.style_dotted)
hline(-100, "-100", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
`},{id:`williams`,name:`Williams %R`,short:`%R`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Williams %R", overlay=false)
w = ta.wpr(input.int(14, "Length"))
plot(w, "%R", color=w > -20 ? #F4F3EF : w < -80 ? #FF4B3E : #E9B949, linewidth=2)
hline(-20, "OB", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
hline(-80, "OS", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
`},{id:`adx`,name:`ADX / DMI`,short:`ADX`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("ADX / DMI", overlay=false)
len = input.int(14, "Length")
[dip, dim, adx] = ta.dmi(len, len)
plot(adx, "ADX", color=adx > 25 ? #E9B949 : #8A8A8E, linewidth=2)
plot(dip, "+DI", color=#F4F3EF)
plot(dim, "-DI", color=#FF4B3E)
hline(25, "Trend", color=color.new(#8A8A8E, 50), linestyle=hline.style_dashed)
`},{id:`ao`,name:`Awesome Oscillator`,short:`AO`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Awesome Oscillator", overlay=false)
ao = ta.sma(hl2, input.int(5, "Fast")) - ta.sma(hl2, input.int(34, "Slow"))
plot(ao, "AO", color=ao > ao[1] ? #F4F3EF : #FF4B3E, style=plot.style_columns)
hline(0, "Zero", color=color.new(#8A8A8E, 60))
`},{id:`mom`,name:`Momentum / ROC`,short:`ROC`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Momentum / ROC", overlay=false)
len = input.int(10, "Length")
r = ta.roc(close, len)
plot(r, "ROC %", color=r >= 0 ? #F4F3EF : #FF4B3E, style=plot.style_area)
hline(0, "Zero", color=color.new(#8A8A8E, 60))
`},{id:`trix`,name:`TRIX`,short:`TRIX`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("TRIX", overlay=false)
len = input.int(18, "Length")
t = 10000 * ta.change(ta.ema(ta.ema(ta.ema(math.log(close), len), len), len))
sig = ta.ema(t, 9)
plot(t, "TRIX", color=#F4F3EF, linewidth=2)
plot(sig, "Signal", color=#FF4B3E)
hline(0, "Zero", color=color.new(#8A8A8E, 60))
`},{id:`tsi`,name:`True Strength Index`,short:`TSI`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("True Strength Index", overlay=false)
t = 100 * ta.tsi(close, input.int(13, "Short"), input.int(25, "Long"))
s = ta.ema(t, input.int(13, "Signal"))
plot(t, "TSI", color=#F4F3EF, linewidth=2)
plot(s, "Signal", color=#FF4B3E)
hline(0, "Zero", color=color.new(#8A8A8E, 60))
`},{id:`ultosc`,name:`Ultimate Oscillator`,short:`UO`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Ultimate Oscillator", overlay=false)
bp = close - math.min(low, close[1])
tr_ = math.max(high, close[1]) - math.min(low, close[1])
f1 = input.int(7, "Fast")
f2 = input.int(14, "Middle")
f3 = input.int(28, "Slow")
a7 = math.sum(bp, f1) / math.sum(tr_, f1)
a14 = math.sum(bp, f2) / math.sum(tr_, f2)
a28 = math.sum(bp, f3) / math.sum(tr_, f3)
uo = 100 * (4 * a7 + 2 * a14 + a28) / 7
plot(uo, "UO", color=uo > 70 ? #F4F3EF : uo < 30 ? #FF4B3E : #E9B949, linewidth=2)
hline(70, "OB", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
hline(30, "OS", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
`},{id:`aroon`,name:`Aroon`,short:`ARN`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Aroon", overlay=false)
len = input.int(14, "Length")
up = 100 * (ta.highestbars(high, len + 1) + len) / len
dn = 100 * (ta.lowestbars(low, len + 1) + len) / len
plot(up, "Aroon Up", color=#F4F3EF, linewidth=2)
plot(dn, "Aroon Down", color=#FF4B3E, linewidth=2)
hline(70, "70", color=color.new(#8A8A8E, 50), linestyle=hline.style_dashed)
hline(30, "30", color=color.new(#8A8A8E, 50), linestyle=hline.style_dashed)
`},{id:`vortex`,name:`Vortex`,short:`VI`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Vortex", overlay=false)
len = input.int(14, "Length")
vmp = math.sum(math.abs(high - low[1]), len)
vmm = math.sum(math.abs(low - high[1]), len)
str_ = math.sum(ta.atr(1), len)
vip = vmp / str_
vim = vmm / str_
plot(vip, "VI+", color=#F4F3EF, linewidth=2)
plot(vim, "VI-", color=#FF4B3E, linewidth=2)
hline(1, "1", color=color.new(#8A8A8E, 60), linestyle=hline.style_dotted)
`},{id:`chop`,name:`Choppiness Index`,short:`CHOP`,pane:!0,tag:`Volatility`,src:`//@version=6
indicator("Choppiness Index", overlay=false)
len = input.int(14, "Length")
ci = 100 * math.log10(math.sum(ta.atr(1), len) / (ta.highest(high, len) - ta.lowest(low, len))) / math.log10(len)
plot(ci, "CHOP", color=ci > 61.8 ? #8A8A8E : ci < 38.2 ? #E9B949 : #F4F3EF, linewidth=2)
hline(61.8, "Choppy", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
hline(38.2, "Trending", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
`},{id:`atrpane`,name:`ATR`,short:`ATR`,pane:!0,tag:`Volatility`,src:`//@version=6
indicator("ATR", overlay=false)
a = ta.atr(input.int(14, "Length"))
plot(a, "ATR", color=a > a[1] ? #F4F3EF : #FF4B3E, style=plot.style_area)
`},{id:`bbwidth`,name:`Bollinger Width`,short:`BBW`,pane:!0,tag:`Volatility`,src:`//@version=6
indicator("Bollinger Width", overlay=false)
w = ta.bbw(close, input.int(20, "Length"), 2.0)
lo = ta.lowest(w, 100)
plot(w, "Width", color=w <= lo * 1.05 ? #E9B949 : #F4F3EF, linewidth=2)
plot(lo, "100-bar low", color=color.new(#8A8A8E, 40), style=plot.style_stepline)
`},{id:`obv`,name:`On-Balance Volume`,short:`OBV`,pane:!0,tag:`Volume`,src:`//@version=6
indicator("On-Balance Volume", overlay=false)
o = ta.obv
m = ta.ema(o, input.int(21, "Signal"))
plot(o, "OBV", color=o > m ? #F4F3EF : #FF4B3E, linewidth=2)
plot(m, "Signal", color=#8A8A8E)
`},{id:`mfi`,name:`Money Flow Index`,short:`MFI`,pane:!0,tag:`Volume`,src:`//@version=6
indicator("Money Flow Index", overlay=false)
m = ta.mfi(hlc3, input.int(14, "Length"))
plot(m, "MFI", color=m > 80 ? #F4F3EF : m < 20 ? #FF4B3E : #E9B949, linewidth=2)
hline(80, "OB", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
hline(20, "OS", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
`},{id:`cmf`,name:`Chaikin Money Flow`,short:`CMF`,pane:!0,tag:`Volume`,src:`//@version=6
indicator("Chaikin Money Flow", overlay=false)
len = input.int(20, "Length")
mfv = high == low ? 0.0 : ((close - low) - (high - close)) / (high - low) * volume
cmf = math.sum(mfv, len) / math.sum(volume, len)
plot(cmf, "CMF", color=cmf >= 0 ? #F4F3EF : #FF4B3E, style=plot.style_area)
hline(0, "Zero", color=color.new(#8A8A8E, 60))
`},{id:`relvol`,name:`Relative Volume`,short:`RVOL`,pane:!0,tag:`Volume`,src:`//@version=6
indicator("Relative Volume", overlay=false)
len = input.int(20, "Average")
rv = volume / ta.sma(volume, len)
plot(rv, "RVOL", color=rv >= 2 ? #E9B949 : close >= open ? #F4F3EF : #FF4B3E, style=plot.style_columns)
hline(1, "Average", color=color.new(#8A8A8E, 50), linestyle=hline.style_dashed)
hline(2, "2x", color=color.new(#E9B949, 50), linestyle=hline.style_dotted)
`},{id:`volosc`,name:`Volume Oscillator`,short:`VO`,pane:!0,tag:`Volume`,src:`//@version=6
indicator("Volume Oscillator", overlay=false)
s = ta.ema(volume, input.int(5, "Short"))
l = ta.ema(volume, input.int(10, "Long"))
vo = 100 * (s - l) / l
plot(vo, "VO", color=vo >= 0 ? #F4F3EF : #FF4B3E, style=plot.style_columns)
hline(0, "Zero", color=color.new(#8A8A8E, 60))
`},{id:`sweeps`,name:`Liquidity Sweeps`,short:`SWP`,pane:!1,tag:`Smart money`,src:`//@version=6
indicator("Liquidity Sweeps", overlay=true)
len = input.int(5, "Swing length")
ph = ta.pivothigh(high, len, len)
pl = ta.pivotlow(low, len, len)
var float lastH = na
var float lastL = na
if not na(ph)
    lastH := ph
if not na(pl)
    lastL := pl
sweepHi = not na(lastH) and high > lastH and close < lastH
sweepLo = not na(lastL) and low < lastL and close > lastL
plotshape(sweepHi ? high : na, "Sweep high", shape.triangledown, location.abovebar, #FF4B3E, text="SWEEP", textcolor=#FF4B3E, size=size.tiny)
plotshape(sweepLo ? low : na, "Sweep low", shape.triangleup, location.belowbar, #F4F3EF, text="SWEEP", textcolor=#F4F3EF, size=size.tiny)
plot(lastH, "Last swing high", color=color.new(#B4A2FF, 50), style=plot.style_stepline)
plot(lastL, "Last swing low", color=color.new(#8AB4F8, 50), style=plot.style_stepline)
`},{id:`eqhl`,name:`Equal Highs & Lows`,short:`EQ`,pane:!1,tag:`Smart money`,src:`//@version=6
indicator("Equal Highs & Lows", overlay=true, max_lines_count=60)
len = input.int(5, "Swing length")
tol = input.float(0.1, "Tolerance (ATR)")
atr = ta.atr(14)
ph = ta.pivothigh(high, len, len)
pl = ta.pivotlow(low, len, len)
var float prevH = na
var int prevHi = na
var float prevL = na
var int prevLi = na
if not na(ph)
    if not na(prevH) and math.abs(ph - prevH) <= tol * atr
        line.new(prevHi, prevH, bar_index - len, ph, color=#FF4B3E, width=1, style=line.style_dotted)
        label.new(bar_index - len, ph, "EQH", style=label.style_label_down, color=color.new(#0A0A0B, 100), textcolor=#FF4B3E, size=size.tiny)
    prevH := ph
    prevHi := bar_index - len
if not na(pl)
    if not na(prevL) and math.abs(pl - prevL) <= tol * atr
        line.new(prevLi, prevL, bar_index - len, pl, color=#F4F3EF, width=1, style=line.style_dotted)
        label.new(bar_index - len, pl, "EQL", style=label.style_label_up, color=color.new(#0A0A0B, 100), textcolor=#F4F3EF, size=size.tiny)
    prevL := pl
    prevLi := bar_index - len
`},{id:`premium`,name:`Premium / Discount`,short:`P/D`,pane:!1,tag:`Smart money`,src:`//@version=6
indicator("Premium / Discount", overlay=true)
len = input.int(60, "Range window")
h = ta.highest(high, len)
l = ta.lowest(low, len)
eq = math.avg(h, l)
top = plot(h, "Range high", color=color.new(#B4A2FF, 40), style=plot.style_stepline)
mid = plot(eq, "Equilibrium", color=color.new(#8A8A8E, 20), style=plot.style_stepline)
bot = plot(l, "Range low", color=color.new(#8AB4F8, 40), style=plot.style_stepline)
fill(top, mid, color=color.new(#B4A2FF, 94), title="Premium")
fill(mid, bot, color=color.new(#8AB4F8, 94), title="Discount")
`},{id:`sessions`,name:`Session VWAP Bands`,short:`SVB`,pane:!1,tag:`Volume`,src:`//@version=6
indicator("Session VWAP Bands", overlay=true)
// Sessions are 4 hours on intraday charts; on 4h and daily charts each bar would be its own session, so they step up to days and weeks.
var int step = na
if bar_index == 1
    step := time - time[1]
big = not na(step) and step >= 4 * 3600000
huge = not na(step) and step >= 86400000
nb = huge ? timeframe.change("W") : big ? timeframe.change("D") : timeframe.change("240")
var float pv = 0.0
var float vv = 0.0
var float pv2 = 0.0
if nb
    pv := 0.0
    vv := 0.0
    pv2 := 0.0
pv += hlc3 * volume
vv += volume
pv2 += hlc3 * hlc3 * volume
vw = pv / vv
sd = math.sqrt(math.max(pv2 / vv - vw * vw, 0))
plot(nb ? na : vw, "VWAP", color=#E9B949, linewidth=2, style=plot.style_linebr)
plot(nb ? na : vw + sd, "+1σ", color=color.new(#8AB4F8, 40), style=plot.style_linebr)
plot(nb ? na : vw - sd, "-1σ", color=color.new(#8AB4F8, 40), style=plot.style_linebr)
`},{id:`rsidiv`,name:`RSI Divergence`,short:`DIV`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("RSI Divergence", overlay=false, max_lines_count=40)
len = input.int(14, "RSI")
piv = input.int(5, "Pivot length")
r = ta.rsi(close, len)
pl = ta.pivotlow(r, piv, piv)
ph = ta.pivothigh(r, piv, piv)
var float prL = na
var float prLp = na
var int prLi = na
var float prH = na
var float prHp = na
var int prHi = na
plot(r, "RSI", color=#F4F3EF, linewidth=2)
hline(70, "OB", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
hline(30, "OS", color=color.new(#8A8A8E, 40), linestyle=hline.style_dashed)
if not na(pl)
    px = low[piv]
    if not na(prL) and px < prLp and pl > prL
        line.new(prLi, prL, bar_index - piv, pl, color=#F4F3EF, width=2)
        label.new(bar_index - piv, pl, "BULL DIV", style=label.style_label_up, color=color.new(#0A0A0B, 100), textcolor=#F4F3EF, size=size.tiny)
    prL := pl
    prLp := px
    prLi := bar_index - piv
if not na(ph)
    px = high[piv]
    if not na(prH) and px > prHp and ph < prH
        line.new(prHi, prH, bar_index - piv, ph, color=#FF4B3E, width=2)
        label.new(bar_index - piv, ph, "BEAR DIV", style=label.style_label_down, color=color.new(#0A0A0B, 100), textcolor=#FF4B3E, size=size.tiny)
    prH := ph
    prHp := px
    prHi := bar_index - piv
`},{id:`utbot`,name:`UT Bot`,short:`UTB`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("UT Bot", overlay=true)
key = input.float(1.0, "Key value")
atrLen = input.int(10, "ATR length")
nLoss = key * ta.atr(atrLen)
var float stop = na
prev = nz(stop[1], close)
stop := close > prev and close[1] > prev ? math.max(prev, close - nLoss) : close < prev and close[1] < prev ? math.min(prev, close + nLoss) : close > prev ? close - nLoss : close + nLoss
var int pos = 0
pos := close[1] < prev and close > prev ? 1 : close[1] > prev and close < prev ? -1 : nz(pos[1], 0)
buy = close > stop and close[1] <= prev
sell = close < stop and close[1] >= prev
plot(stop, "Trailing stop", color=pos >= 0 ? #F4F3EF : #FF4B3E, linewidth=2)
plotshape(buy ? stop : na, "Buy", shape.labelup, location.absolute, #F4F3EF, text="BUY", textcolor=#0A0A0B, size=size.tiny)
plotshape(sell ? stop : na, "Sell", shape.labeldown, location.absolute, #FF4B3E, text="SELL", textcolor=#0A0A0B, size=size.tiny)
`},{id:`ssl`,name:`SSL Channel`,short:`SSL`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("SSL Channel", overlay=true)
len = input.int(10, "Length")
smaHigh = ta.sma(high, len)
smaLow = ta.sma(low, len)
var int hlv = 0
hlv := close > smaHigh ? 1 : close < smaLow ? -1 : nz(hlv[1], 0)
sslDown = hlv < 0 ? smaHigh : smaLow
sslUp = hlv < 0 ? smaLow : smaHigh
u = plot(sslUp, "SSL up", color=#F4F3EF, linewidth=2)
d = plot(sslDown, "SSL down", color=#FF4B3E, linewidth=2)
fill(u, d, color=hlv > 0 ? color.new(#F4F3EF, 92) : color.new(#FF4B3E, 90), title="Channel")
`},{id:`rangefilter`,name:`Range Filter`,short:`RF`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("Range Filter", overlay=true)
per = input.int(100, "Sampling period")
mult = input.float(3.0, "Range multiplier")
avrng = ta.ema(math.abs(close - close[1]), per)
smrng = ta.ema(avrng, per * 2 - 1) * mult
var float filt = na
f1 = nz(filt[1], close)
filt := close > f1 ? (close - smrng < f1 ? f1 : close - smrng) : (close + smrng > f1 ? f1 : close + smrng)
hband = filt + smrng
lband = filt - smrng
var int upward = 0
var int downward = 0
upward := filt > f1 ? nz(upward[1]) + 1 : filt < f1 ? 0 : nz(upward[1])
downward := filt < f1 ? nz(downward[1]) + 1 : filt > f1 ? 0 : nz(downward[1])
fcol = upward > 0 ? #F4F3EF : downward > 0 ? #FF4B3E : #8A8A8E
f = plot(filt, "Filter", color=fcol, linewidth=2)
h = plot(hband, "High band", color=color.new(#8A8A8E, 70))
l = plot(lband, "Low band", color=color.new(#8A8A8E, 70))
fill(h, f, color=color.new(#F4F3EF, 93), title="Upper")
fill(l, f, color=color.new(#FF4B3E, 91), title="Lower")
longCond = close > filt and close > close[1] and upward > 0
shortCond = close < filt and close < close[1] and downward > 0
var int cond = 0
cond := longCond ? 1 : shortCond ? -1 : nz(cond[1])
plotshape(longCond and nz(cond[1]) != 1 ? lband : na, "Buy", shape.labelup, location.absolute, #F4F3EF, text="BUY", textcolor=#0A0A0B, size=size.tiny)
plotshape(shortCond and nz(cond[1]) != -1 ? hband : na, "Sell", shape.labeldown, location.absolute, #FF4B3E, text="SELL", textcolor=#0A0A0B, size=size.tiny)
`},{id:`trendmagic`,name:`Trend Magic`,short:`TM`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("Trend Magic", overlay=true)
cciLen = input.int(20, "CCI length")
atrLen = input.int(5, "ATR length")
mult = input.float(1.0, "ATR multiplier")
up = low - ta.atr(atrLen) * mult
dn = high + ta.atr(atrLen) * mult
cci = ta.cci(close, cciLen)
var float mt = na
m1 = nz(mt[1], close)
mt := cci >= 0 ? (up < m1 ? m1 : up) : (dn > m1 ? m1 : dn)
plot(mt, "Trend Magic", color=cci >= 0 ? #8AB4F8 : #FF4B3E, linewidth=2)
`},{id:`kama`,name:`Kaufman Adaptive MA`,short:`KAMA`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("Kaufman Adaptive MA", overlay=true)
len = input.int(10, "Length")
fastL = input.int(2, "Fast")
slowL = input.int(30, "Slow")
change = math.abs(close - close[len])
vol = ta.sma(math.abs(close - close[1]), len) * len
er = vol != 0 ? change / vol : 0
fast = 2 / (fastL + 1)
slow = 2 / (slowL + 1)
sc = math.pow(er * (fast - slow) + slow, 2)
var float kama = na
k1 = nz(kama[1], close)
kama := k1 + sc * (close - k1)
plot(kama, "KAMA", color=kama >= k1 ? #F4F3EF : #FF4B3E, linewidth=2)
`},{id:`mcginley`,name:`McGinley Dynamic`,short:`MCG`,pane:!1,tag:`Trend`,src:`//@version=6
indicator("McGinley Dynamic", overlay=true)
len = input.int(14, "Length")
var float md = na
m1 = nz(md[1], close)
md := m1 + (close - m1) / (len * math.pow(close / m1, 4))
plot(md, "McGinley", color=#8AB4F8, linewidth=2)
`},{id:`nwe`,name:`Nadaraya-Watson Envelope`,short:`NWE`,pane:!1,tag:`Volatility`,src:`//@version=6
indicator("Nadaraya-Watson Envelope", overlay=true)
h = input.float(8.0, "Bandwidth")
mult = input.float(3.0, "Multiplier")
win = input.int(60, "Window")
float sum = 0.0
float sumw = 0.0
for i = 0 to win - 1
    w = math.exp(-(i * i) / (2 * h * h))
    sum += nz(close[i], close) * w
    sumw += w
nw = sum / sumw
mae = ta.sma(math.abs(close - nw), win) * mult
u = plot(nw + mae, "Upper", color=#8AB4F8)
l = plot(nw - mae, "Lower", color=#B4A2FF)
plot(nw, "Estimate", color=#F4F3EF, linewidth=2)
fill(u, l, color=color.new(#8AB4F8, 94), title="Envelope")
`},{id:`qqe`,name:`QQE`,short:`QQE`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("QQE", overlay=false)
rsiLen = input.int(14, "RSI length")
sf = input.int(5, "Smoothing")
qq = input.float(4.238, "QQE factor")
wilders = rsiLen * 2 - 1
rsiMa = ta.ema(ta.rsi(close, rsiLen), sf)
atrRsi = math.abs(nz(rsiMa[1], rsiMa) - rsiMa)
dar = ta.ema(ta.ema(atrRsi, wilders), wilders) * qq
newShort = rsiMa + dar
newLong = rsiMa - dar
var float longband = na
var float shortband = na
var int trend = 1
lb1 = nz(longband[1], newLong)
sb1 = nz(shortband[1], newShort)
longband := nz(rsiMa[1], rsiMa) > lb1 and rsiMa > lb1 ? math.max(lb1, newLong) : newLong
shortband := nz(rsiMa[1], rsiMa) < sb1 and rsiMa < sb1 ? math.min(sb1, newShort) : newShort
trend := ta.cross(rsiMa, sb1) ? 1 : ta.cross(lb1, rsiMa) ? -1 : nz(trend[1], 1)
fast = trend == 1 ? longband : shortband
hline(50, "Mid", color=color.new(#8A8A8E, 40))
plot(rsiMa, "RSI MA", color=#F4F3EF, linewidth=2)
plot(fast, "ATR trail", color=trend == 1 ? #8AB4F8 : #FF4B3E)
`},{id:`wae`,name:`Waddah Attar Explosion`,short:`WAE`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Waddah Attar Explosion", overlay=false)
sens = input.int(150, "Sensitivity")
fastLen = input.int(20, "Fast EMA")
slowLen = input.int(40, "Slow EMA")
chLen = input.int(20, "Channel length")
mult = input.float(2.0, "BB multiplier")
macd = ta.ema(close, fastLen) - ta.ema(close, slowLen)
t1 = (macd - nz(macd[1], macd)) * sens
[bbM, bbU, bbL] = ta.bb(close, chLen, mult)
e1 = bbU - bbL
dz = ta.rma(ta.tr(true), 100) * 3.7
plot(t1 >= 0 ? t1 : 0, "Up trend", color=t1 > nz(t1[1]) ? #F4F3EF : color.new(#F4F3EF, 50), style=plot.style_columns)
plot(t1 < 0 ? -t1 : 0, "Down trend", color=t1 < nz(t1[1]) ? #FF4B3E : color.new(#FF4B3E, 50), style=plot.style_columns)
plot(e1, "Explosion", color=#E9B949, linewidth=2)
plot(dz, "Dead zone", color=#8A8A8E)
`},{id:`stc`,name:`Schaff Trend Cycle`,short:`STC`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Schaff Trend Cycle", overlay=false)
length = input.int(10, "Length")
fastLen = input.int(23, "Fast")
slowLen = input.int(50, "Slow")
factor = 0.5
macd = ta.ema(close, fastLen) - ta.ema(close, slowLen)
v1 = ta.lowest(macd, length)
v2 = ta.highest(macd, length) - v1
var float f1 = 0.0
var float pf = 0.0
var float f2 = 0.0
var float pff = 0.0
f1 := v2 > 0 ? (macd - v1) / v2 * 100 : nz(f1[1])
pf := nz(pf[1]) + factor * (f1 - nz(pf[1]))
v3 = ta.lowest(pf, length)
v4 = ta.highest(pf, length) - v3
f2 := v4 > 0 ? (pf - v3) / v4 * 100 : nz(f2[1])
pff := nz(pff[1]) + factor * (f2 - nz(pff[1]))
hline(75, "Upper", color=color.new(#8A8A8E, 40))
hline(25, "Lower", color=color.new(#8A8A8E, 40))
plot(pff, "STC", color=pff >= nz(pff[1]) ? #F4F3EF : #FF4B3E, linewidth=2)
`},{id:`fisher`,name:`Fisher Transform`,short:`FISH`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Fisher Transform", overlay=false)
len = input.int(9, "Length")
mid = (high + low) / 2
hi = ta.highest(mid, len)
lo = ta.lowest(mid, len)
var float val = 0.0
var float fish = 0.0
raw = hi - lo > 0 ? (mid - lo) / (hi - lo) - 0.5 : 0
val := 0.66 * raw + 0.67 * nz(val[1])
v = math.max(math.min(val, 0.999), -0.999)
fish := 0.5 * math.log((1 + v) / (1 - v)) + 0.5 * nz(fish[1])
hline(0, "Zero", color=color.new(#8A8A8E, 40))
hline(1.5, "High", color=color.new(#8A8A8E, 60))
hline(-1.5, "Low", color=color.new(#8A8A8E, 60))
plot(fish, "Fisher", color=#F4F3EF, linewidth=2)
plot(nz(fish[1]), "Trigger", color=#FF4B3E)
`},{id:`smi`,name:`Stochastic Momentum Index`,short:`SMI`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Stochastic Momentum Index", overlay=false)
a = input.int(10, "%K length")
b = input.int(3, "%D length")
hh = ta.highest(high, a)
ll = ta.lowest(low, a)
rdiff = close - (hh + ll) / 2
rdiffS = ta.ema(ta.ema(rdiff, b), b)
diffS = ta.ema(ta.ema(hh - ll, b), b)
smi = diffS != 0 ? rdiffS / (diffS / 2) * 100 : 0
sig = ta.ema(smi, b)
hline(40, "Over", color=color.new(#8A8A8E, 40))
hline(-40, "Under", color=color.new(#8A8A8E, 40))
plot(smi, "SMI", color=#F4F3EF, linewidth=2)
plot(sig, "Signal", color=#FF4B3E)
`},{id:`crsi`,name:`Connors RSI`,short:`CRSI`,pane:!0,tag:`Momentum`,src:`//@version=6
indicator("Connors RSI", overlay=false)
lenRsi = input.int(3, "RSI length")
lenStreak = input.int(2, "Streak RSI")
lenRank = input.int(100, "ROC rank")
var int streak = 0
s1 = nz(streak[1], 0)
streak := close > close[1] ? (s1 > 0 ? s1 + 1 : 1) : close < close[1] ? (s1 < 0 ? s1 - 1 : -1) : 0
rsi1 = ta.rsi(close, lenRsi)
rsi2 = ta.rsi(streak, lenStreak)
rank = ta.percentrank(ta.roc(close, 1), lenRank)
crsi = (rsi1 + rsi2 + rank) / 3
hline(90, "Over", color=color.new(#8A8A8E, 40))
hline(10, "Under", color=color.new(#8A8A8E, 40))
plot(crsi, "Connors RSI", color=#F4F3EF, linewidth=2)
`},{id:`cvd`,name:`Cumulative Volume Delta`,short:`CVD`,pane:!0,tag:`Volume`,src:`//@version=6
indicator("Cumulative Volume Delta", overlay=false)
rng = high - low
delta = rng > 0 ? volume * (close - open) / rng : 0.0
var float cvd = 0.0
cvd := nz(cvd[1]) + delta
hline(0, "Zero", color=color.new(#8A8A8E, 40))
plot(cvd, "CVD", color=cvd >= nz(cvd[1]) ? #F4F3EF : #FF4B3E, style=plot.style_area)
`},{id:`elder`,name:`Elder Impulse`,short:`ELD`,pane:!1,tag:`Look`,src:`//@version=6
indicator("Elder Impulse", overlay=true)
e = ta.ema(close, input.int(13, "EMA"))
[m, s, h] = ta.macd(close, input.int(12, "MACD fast"), input.int(26, "MACD slow"), input.int(9, "MACD signal"))
up = e > nz(e[1], e) and h > nz(h[1], h)
dn = e < nz(e[1], e) and h < nz(h[1], h)
barcolor(up ? #F4F3EF : dn ? #FF4B3E : #8A8A8E)
plot(e, "EMA", color=#8AB4F8)
`}]
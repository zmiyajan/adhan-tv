// Renders the TV banner (320×180) and launcher icon (96×96, xhdpi) with CoreText, which shapes Arabic properly.
// Usage: swift tools/icons.swift   (run from the repository root)
import AppKit
import CoreText

let gold = NSColor(red: 187 / 255, green: 162 / 255, blue: 122 / 255, alpha: 1)
let amiri = URL(fileURLWithPath: "app/src/main/res/font/amiri_bold.ttf")
CTFontManagerRegisterFontsForURL(amiri as CFURL, .process, nil)

func render(_ w: Int, _ h: Int, _ text: String, size: CGFloat, rule: Bool, to path: String) {
    let rep = NSBitmapImageRep(bitmapDataPlanes: nil, pixelsWide: w, pixelsHigh: h, bitsPerSample: 8,
                               samplesPerPixel: 4, hasAlpha: true, isPlanar: false, colorSpaceName: .deviceRGB,
                               bytesPerRow: 0, bitsPerPixel: 0)!
    NSGraphicsContext.current = NSGraphicsContext(bitmapImageRep: rep)
    NSColor(red: 12 / 255, green: 12 / 255, blue: 13 / 255, alpha: 1).setFill()
    NSRect(x: 0, y: 0, width: w, height: h).fill()
    let font = NSFont(name: "Amiri-Bold", size: size) ?? NSFont.boldSystemFont(ofSize: size)
    let s = NSAttributedString(string: text, attributes: [.font: font, .foregroundColor: gold])
    let b = s.size()
    let y = (CGFloat(h) - b.height) / 2 + (rule ? 8 : 2)
    s.draw(at: NSPoint(x: (CGFloat(w) - b.width) / 2, y: y))
    if rule {
        gold.withAlphaComponent(0.55).setFill()
        NSRect(x: CGFloat(w) / 2 - 40, y: y - 6, width: 80, height: 2).fill()
    }
    NSGraphicsContext.current = nil
    try! rep.representation(using: .png, properties: [:])!.write(to: URL(fileURLWithPath: path))
}

render(320, 180, "أذان التلفاز", size: 52, rule: true, to: "app/src/main/res/drawable/banner.png")
render(96, 96, "أذان", size: 34, rule: false, to: "app/src/main/res/mipmap-xhdpi/ic_launcher.png")

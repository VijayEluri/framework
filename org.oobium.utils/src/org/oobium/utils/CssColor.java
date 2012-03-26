package org.oobium.utils;

import static java.lang.Math.*;

import java.util.HashMap;
import java.util.Map;

public class CssColor {

	private static final Map<String, Integer> colors;
	static {
		colors = new HashMap<String, Integer>();
        colors.put("aliceblue", 0xf0f8ff);
        colors.put("antiquewhite", 0xfaebd7);
        colors.put("aqua", 0x00ffff);
        colors.put("aquamarine", 0x7fffd4);
        colors.put("azure", 0xf0ffff);
        colors.put("beige", 0xf5f5dc);
        colors.put("bisque", 0xffe4c4);
        colors.put("black", 0x000000);
        colors.put("blanchedalmond", 0xffebcd);
        colors.put("blue", 0x0000ff);
        colors.put("blueviolet", 0x8a2be2);
        colors.put("brown", 0xa52a2a);
        colors.put("burlywood", 0xdeb887);
        colors.put("cadetblue", 0x5f9ea0);
        colors.put("chartreuse", 0x7fff00);
        colors.put("chocolate", 0xd2691e);
        colors.put("coral", 0xff7f50);
        colors.put("cornflowerblue", 0x6495ed);
        colors.put("cornsilk", 0xfff8dc);
        colors.put("crimson", 0xdc143c);
        colors.put("cyan", 0x00ffff);
        colors.put("darkblue", 0x00008b);
        colors.put("darkcyan", 0x008b8b);
        colors.put("darkgoldenrod", 0xb8860b);
        colors.put("darkgray", 0xa9a9a9);
        colors.put("darkgrey", 0xa9a9a9);
        colors.put("darkgreen", 0x006400);
        colors.put("darkkhaki", 0xbdb76b);
        colors.put("darkmagenta", 0x8b008b);
        colors.put("darkolivegreen", 0x556b2f);
        colors.put("darkorange", 0xff8c00);
        colors.put("darkorchid", 0x9932cc);
        colors.put("darkred", 0x8b0000);
        colors.put("darksalmon", 0xe9967a);
        colors.put("darkseagreen", 0x8fbc8f);
        colors.put("darkslateblue", 0x483d8b);
        colors.put("darkslategray", 0x2f4f4f);
        colors.put("darkslategrey", 0x2f4f4f);
        colors.put("darkturquoise", 0x00ced1);
        colors.put("darkviolet", 0x9400d3);
        colors.put("deeppink", 0xff1493);
        colors.put("deepskyblue", 0x00bfff);
        colors.put("dimgray", 0x696969);
        colors.put("dimgrey", 0x696969);
        colors.put("dodgerblue", 0x1e90ff);
        colors.put("firebrick", 0xb22222);
        colors.put("floralwhite", 0xfffaf0);
        colors.put("forestgreen", 0x228b22);
        colors.put("fuchsia", 0xff00ff);
        colors.put("gainsboro", 0xdcdcdc);
        colors.put("ghostwhite", 0xf8f8ff);
        colors.put("gold", 0xffd700);
        colors.put("goldenrod", 0xdaa520);
        colors.put("gray", 0x808080);
        colors.put("grey", 0x808080);
        colors.put("green", 0x008000);
        colors.put("greenyellow", 0xadff2f);
        colors.put("honeydew", 0xf0fff0);
        colors.put("hotpink", 0xff69b4);
        colors.put("indianred", 0xcd5c5c);
        colors.put("indigo", 0x4b0082);
        colors.put("ivory", 0xfffff0);
        colors.put("khaki", 0xf0e68c);
        colors.put("lavender", 0xe6e6fa);
        colors.put("lavenderblush", 0xfff0f5);
        colors.put("lawngreen", 0x7cfc00);
        colors.put("lemonchiffon", 0xfffacd);
        colors.put("lightblue", 0xadd8e6);
        colors.put("lightcoral", 0xf08080);
        colors.put("lightcyan", 0xe0ffff);
        colors.put("lightgoldenrodyellow", 0xfafad2);
        colors.put("lightgray", 0xd3d3d3);
        colors.put("lightgrey", 0xd3d3d3);
        colors.put("lightgreen", 0x90ee90);
        colors.put("lightpink", 0xffb6c1);
        colors.put("lightsalmon", 0xffa07a);
        colors.put("lightseagreen", 0x20b2aa);
        colors.put("lightskyblue", 0x87cefa);
        colors.put("lightslategray", 0x778899);
        colors.put("lightslategrey", 0x778899);
        colors.put("lightsteelblue", 0xb0c4de);
        colors.put("lightyellow", 0xffffe0);
        colors.put("lime", 0x00ff00);
        colors.put("limegreen", 0x32cd32);
        colors.put("linen", 0xfaf0e6);
        colors.put("magenta", 0xff00ff);
        colors.put("maroon", 0x800000);
        colors.put("mediumaquamarine", 0x66cdaa);
        colors.put("mediumblue", 0x0000cd);
        colors.put("mediumorchid", 0xba55d3);
        colors.put("mediumpurple", 0x9370d8);
        colors.put("mediumseagreen", 0x3cb371);
        colors.put("mediumslateblue", 0x7b68ee);
        colors.put("mediumspringgreen", 0x00fa9a);
        colors.put("mediumturquoise", 0x48d1cc);
        colors.put("mediumvioletred", 0xc71585);
        colors.put("midnightblue", 0x191970);
        colors.put("mintcream", 0xf5fffa);
        colors.put("mistyrose", 0xffe4e1);
        colors.put("moccasin", 0xffe4b5);
        colors.put("navajowhite", 0xffdead);
        colors.put("navy", 0x000080);
        colors.put("oldlace", 0xfdf5e6);
        colors.put("olive", 0x808000);
        colors.put("olivedrab", 0x6b8e23);
        colors.put("orange", 0xffa500);
        colors.put("orangered", 0xff4500);
        colors.put("orchid", 0xda70d6);
        colors.put("palegoldenrod", 0xeee8aa);
        colors.put("palegreen", 0x98fb98);
        colors.put("paleturquoise", 0xafeeee);
        colors.put("palevioletred", 0xd87093);
        colors.put("papayawhip", 0xffefd5);
        colors.put("peachpuff", 0xffdab9);
        colors.put("peru", 0xcd853f);
        colors.put("pink", 0xffc0cb);
        colors.put("plum", 0xdda0dd);
        colors.put("powderblue", 0xb0e0e6);
        colors.put("purple", 0x800080);
        colors.put("red", 0xff0000);
        colors.put("rosybrown", 0xbc8f8f);
        colors.put("royalblue", 0x4169e1);
        colors.put("saddlebrown", 0x8b4513);
        colors.put("salmon", 0xfa8072);
        colors.put("sandybrown", 0xf4a460);
        colors.put("seagreen", 0x2e8b57);
        colors.put("seashell", 0xfff5ee);
        colors.put("sienna", 0xa0522d);
        colors.put("silver", 0xc0c0c0);
        colors.put("skyblue", 0x87ceeb);
        colors.put("slateblue", 0x6a5acd);
        colors.put("slategray", 0x708090);
        colors.put("slategrey", 0x708090);
        colors.put("snow", 0xfffafa);
        colors.put("springgreen", 0x00ff7f);
        colors.put("steelblue", 0x4682b4);
        colors.put("tan", 0xd2b48c);
        colors.put("teal", 0x008080);
        colors.put("thistle", 0xd8bfd8);
        colors.put("tomato", 0xff6347);
        colors.put("turquoise", 0x40e0d0);
        colors.put("violet", 0xee82ee);
        colors.put("wheat", 0xf5deb3);
        colors.put("white", 0xffffff);
        colors.put("whitesmoke", 0xf5f5f5);
        colors.put("yellow", 0xffff00);
        colors.put("yellowgreen", 0x9acd32);
	}

	public static CssColor color(String color) {
		return new CssColor(value(color), 100);
	}
	
	public static CssColor color(String color, float a) {
		return new CssColor(value(color), (int) (a*100));
	}
	
	public static CssColor color(String color, int a) {
		return new CssColor(value(color), a);
	}
	
	public static CssColor hsl(float h, float s, float l) {
		return hsl(h, s, l, 100);
	}
	
	public static CssColor hsl(float h, float s, float l, float a) {
		return hsl(h, s, l, (int) (a*100));
	}
	
	/**
	 * Converts an HSL color value to RGB. Conversion formula
	 * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
	 * Source code adapted from http://mjijackson.com/2008/02/rgb-to-hsl-and-rgb-to-hsv-color-model-conversion-algorithms-in-javascript.
	 * Assumes h, s, and l are contained in the set [0, 1] and
	 * returns r, g, and b in the set [0, 255].
	 *
	 * @param h The hue
	 * @param s The saturation
	 * @param l The lightness
	 * @return a new {@link CssColor} object with its value set from the given HSL values
	 */
	public static CssColor hsl(float h, float s, float l, int a) {
	    float r, g, b;

        float q = (l < 0.5) ? (l * (1 + s)) : (l + s - (l * s));
        float p = (2 * l) - q;
        r = hue2rgb(p, q, h + 1f/3);
        g = hue2rgb(p, q, h);
        b = hue2rgb(p, q, h - 1f/3);

	    return rgb( (int) (r * 255), (int) (g * 255), (int) (b * 255), a );
	}
	
	public static CssColor hsl(float[] hsl) {
		return hsl(hsl[0], hsl[1], hsl[2], 100);
	}
	
	public static CssColor hsl(float[] hsl, float a) {
		return hsl(hsl[0], hsl[1], hsl[2], a);
	}
	
	public static CssColor hsl(float[] hsl, int a) {
		return hsl(hsl[0], hsl[1], hsl[2], a);
	}

    private static float hue2rgb(float p, float q, float t){
        if(t < 0) t += 1;
        if(t > 1) t -= 1;
        if(t < 1f/6) return p + (q - p) * 6 * t;
        if(t < 1f/2) return q;
        if(t < 2f/3) return p + (q - p) * (2f/3 - t) * 6;
        return p;
    }

	public static void main(String[] args) {
//		String color = "green";
//		System.out.println("value: " + value(color) + ", color" + color(color));
//		System.out.println(rgb(10,10,9).r(10).g(0).b(120));
//		System.out.println(color("#ff0a").b());
//		System.out.println(color("#fff").subtract(color("red")));
//		System.out.println(rgb(12,255,13).a(50));
//		System.out.println(color("#aaccdd"));
//		System.out.println(color("#aaccdd").r() + ", " + color("#aaccdd").g() + ", " + color("#aaccdd").b());
//		float[] hsl = color("#aaccdd").hsl();
//		System.out.println(StringUtils.asString(hsl));
//		System.out.println(hsl(hsl));
//		System.out.println(color("#acd").greyscale());
//		System.out.println(StringUtils.asString(hsl(hsl).greyscale().hsl()));
//		System.out.println(StringUtils.asString(hsl(hsl).s(0)));
		System.out.println(color("#333").mix(color("#111", 50), 0.5f));
	}

	public static String[] predefined() {
		return colors.keySet().toArray(new String[colors.size()]);
	}
	
	public static CssColor rgb(int r, int g, int b) {
		return new CssColor(b + (g << 8) + (r << 16), 100);
	}
	
	public static CssColor rgb(int r, int g, int b, float a) {
		return new CssColor(b + (g << 8) + (r << 16), (int) (a*100));
	}
	
	public static CssColor rgb(int r, int g, int b, int a) {
		return new CssColor(b + (g << 8) + (r << 16), a);
	}
	
	public static CssColor rgb(int[] rgb) {
		return rgb(rgb[0], rgb[1], rgb[2], 100);
	}
	
	public static CssColor rgb(int[] rgb, float a) {
		return rgb(rgb[0], rgb[1], rgb[2], a);
	}
	
	public static CssColor rgb(int[] rgb, int a) {
		return rgb(rgb[0], rgb[1], rgb[2], a);
	}

	public static int value(String color) {
		if(color == null || color.length() == 0) {
			return 0;
		} else {
			Integer i = colors.get(color);
			if(i == null) {
				String s = ((color.charAt(0) == '#') ? color.substring(1) : color).trim();
				if(s.length() == 3) {
					char[] ca = s.toCharArray();
					s = new String(new char[] {ca[0],ca[0], ca[1],ca[1], ca[2],ca[2]});
				}
				i = Integer.parseInt(s, 16);
			}
			return i.intValue();
		}
	}

	
	private final int rgb;
	private final int a;
	
	private CssColor(int rgb, int a) {
		this.rgb = limit(rgb, 0, 0xffffff);
		this.a = limit(a, 0, 100);
	}
	
	public int a() {
		return a;
	}
	
	public CssColor a(float a) {
		int i = limit((int) (a*100), 0, 100);
		return new CssColor(rgb, i);
	}
	
	public CssColor a(int a) {
		a = limit(a, 0, 100);
		return new CssColor(rgb, a);
	}
	
	public CssColor add(CssColor other) {
		int[] rgb = new int[] {
				limit(r() + other.r(), 0, 255),
				limit(g() + other.g(), 0, 255),
				limit(b() + other.b(), 0, 255)
		};
		return rgb(rgb, a);
	}
	
	public CssColor add(int amount) {
		return add(new CssColor(amount, a));
	}
	
	public CssColor add(String amount) {
		return add(new CssColor(value(amount), a));
	}
	
	public float alpha() {
		return (float) a / 100;
	}

	public int b() {
		return rgb & 0x0000ff;
	}

	public CssColor b(int b) {
		b = limit(b, 0, 255);
		return new CssColor((rgb & 0xffff00) + b, a);
	}
	
	public CssColor darken(float amount) {
		return hsl(2, -amount);
	}
	
	public CssColor desaturate(float amount) {
		return hsl(1, -amount);
	}
	
	public CssColor fadeIn(float amount) {
		return a(a + (amount*255));
	}
	
	public CssColor fadeOut(float amount) {
		return a(a - (amount*255));
	}
	
	public int g() {
		return (rgb & 0x00ff00) >> 8;
	}
	
	public CssColor g(int g) {
		g = limit(g, 0, 255);
		return new CssColor((rgb & 0xff00ff) + (g << 8), a);
	}
	
	public CssColor greyscale() {
		return s(0.0f);
	}
	
	public float h() {
		return hsl()[0];
	}
	
	public CssColor h(float h) {
		return hsl(h, s(), l());
	}

	/**
	 * Converts an RGB color value to HSL. Conversion formula
	 * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
	 * Source code adapted from http://mjijackson.com/2008/02/rgb-to-hsl-and-rgb-to-hsv-color-model-conversion-algorithms-in-javascript.
	 * Assumes r, g, and b are contained in the set [0, 255] and
	 * returns h, s, and l in the set [0, 1].
	 *
	 * @return a float[] containing the HSL values
	 */
	public float[] hsl() {
		float r = (float) r() / 255;
		float g = (float) g() / 255;
		float b = (float) b() / 255;
	    float max = max(r, max(g, b));
	    float min = min(r, min(g, b));
	    float h, s, l = (max + min) / 2;

	    if(max == min) {
	        h = s = 0; // achromatic
	    } else {
	        float d = max - min;
	        s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
	        if(max == r) {
	        	h = (g - b) / d + (g < b ? 6 : 0);
	        }
	        else if(max == g) {
	        	h = (b - r) / d + 2;
	        }
	        else if(max == b) {
	            h = (r - g) / d + 4;
	        }
	        else {
	        	throw new IllegalAccessError();
	        }
	        h = h / 6;
	    }

	    return new float[] { h, s, l };
	}
	
	private CssColor hsl(int field, float amount) {
		float[] hsl = hsl();
		hsl[field] = limit(field, hsl[field] + amount);
		return hsl(hsl, a);
	}
	
	public float l() {
		return hsl()[2];
	}
	
	public CssColor l(float l) {
		return hsl(h(), s(), l);
	}
	
	public CssColor lighten(float amount) {
		return hsl(2, amount);
	}
	
	private float limit(int field, float value) {
		if(value < 0) return 0;
		if(value > 1.0f) return 1.0f;
		return value;
	}
	
	private int limit(int val, int min, int max) {
		if(val < min) return min;
		if(val > max) return max;
		return val;
	}
	
	//
    // Copyright (c) 2006-2009 Hampton Catlin, Nathan Weizenbaum, and Chris Eppstein
    // http://sass-lang.com
    //
    public CssColor mix(CssColor other, float weight) {
        float w = weight * 2 - 1;
        float a = alpha() - other.alpha();

        float w1 = (((w * a == -1) ? w : (w + a) / (1 + w * a)) + 1) / 2.0f;
        float w2 = 1 - w1;

        int[] rgb = new int[] {
        		limit((int) ((r() * w1) + (other.r() * w2)), 0, 255),
        		limit((int) ((g() * w1) + (other.g() * w2)), 0, 255),
        		limit((int) ((b() * w1) + (other.b() * w2)), 0, 255)
        };

        float alpha = alpha() * weight + other.alpha() * (1 - weight);

        return rgb(rgb, alpha);
    }
	
	public int r() {
		return (rgb & 0xff0000) >> 16;
	}
	
	public CssColor r(int r) {
		r = limit(r, 0, 255);
		return new CssColor((rgb & 0x00ffff) + (r << 16), a);
	}
	
	public int[] rgb() {
		return new int[] { r(), g(), b() };
	}
	
	public float s() {
		return hsl()[1];
	}
	
	public CssColor s(float s) {
		return hsl(h(), s, l());
	}
	
	public CssColor saturate(float amount) {
		return hsl(1, amount);
	}

	public CssColor spin(int degrees) {
		float d = ((float) (degrees % 360)) / 360;
		if(d < 0) d += 1.0f;
		return hsl(0, d);
	}
	
	public CssColor subtract(CssColor other) {
		int[] rgb = new int[] {
				limit(r() - other.r(), 0, 255),
				limit(g() - other.g(), 0, 255),
				limit(b() - other.b(), 0, 255)
		};
		return rgb(rgb, a);
	}

    public CssColor subtract(int amount) {
		return subtract(new CssColor(amount, a));
	}
	
	public CssColor subtract(String amount) {
		return subtract(new CssColor(value(amount), a));
	}
	
	@Override
	public String toString() {
		if(a < 100) {
			StringBuilder sb = new StringBuilder();
			sb.append("rgba(");
			sb.append(r()).append(',');
			sb.append(g()).append(',');
			sb.append(b()).append(',');
			if(a == 100) sb.append("1.0");
			else if(a == 0) sb.append("0.0");
			else if(a < 10) sb.append("0.0").append(a);
			else if(a % 10 == 0) sb.append("0.").append(a/10);
			else sb.append("0.").append(a);
			sb.append(')');
			return sb.toString();
		} else {
			String hex = Integer.toHexString(rgb);
			if(hex.length() == 6) {
				return "#" + hex;
			}
			StringBuilder sb = new StringBuilder();
			sb.append('#');
			for(int i = hex.length(); i < 6; i++) {
				sb.append('0');
			}
			sb.append(hex);
			return sb.toString();
		}
	}
	
}

import net.runelite.mapping.ObfuscatedGetter;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("vw")
public class class567 {
	@ObfuscatedName("ab")
	boolean field5524;
	@ObfuscatedName("ay")
	@ObfuscatedGetter(
		intValue = 809284913
	)
	public int field5518;
	@ObfuscatedName("an")
	public final int[] field5517;
	@ObfuscatedName("au")
	public final int[] field5520;
	@ObfuscatedName("ax")
	public final int[] field5521;
	@ObfuscatedName("ao")
	public final int[] field5519;
	@ObfuscatedName("am")
	public final int[] field5523;
	@ObfuscatedName("ac")
	public final int[] field5527;
	@ObfuscatedName("ae")
	public final String[] field5525;
	@ObfuscatedName("ad")
	public final String[] field5522;
	@ObfuscatedName("aq")
	@ObfuscatedSignature(
		descriptor = "[Lvw;"
	)
	public final class567[] field5526;
	@ObfuscatedName("al")
	public final boolean[] field5528;
	@ObfuscatedName("aj")
	@ObfuscatedGetter(
		intValue = 615979633
	)
	int field5529;
	@ObfuscatedName("as")
	@ObfuscatedGetter(
		intValue = -1392142687
	)
	int field5530;
	@ObfuscatedName("aw")
	@ObfuscatedGetter(
		intValue = -18684457
	)
	int field5531;
	@ObfuscatedName("af")
	int field5532;
	@ObfuscatedName("aa")
	@ObfuscatedGetter(
		intValue = -2064708497
	)
	int field5533;
	@ObfuscatedName("ah")
	@ObfuscatedGetter(
		intValue = 1117737661
	)
	int field5534;
	@ObfuscatedName("ag")
	@ObfuscatedGetter(
		longValue = 3093000226942863233L
	)
	long field5535;

	public class567(boolean var1) {
		this.field5518 = 0;
		int var2 = var1 ? 500 : 20;
		this.field5524 = var1;
		this.field5517 = new int[var2];
		this.field5520 = new int[var2];
		this.field5521 = new int[var2];
		this.field5519 = new int[var2];
		this.field5523 = new int[var2];
		this.field5527 = new int[var2];
		this.field5525 = new String[var2];
		this.field5522 = new String[var2];
		this.field5526 = new class567[var2];
		this.field5528 = new boolean[var2];
	}

	@ObfuscatedName("ab")
	@ObfuscatedSignature(
		descriptor = "(IB)Ljava/lang/String;",
		garbageValue = "67"
	)
	public final String method10250(int var1) {
		if (var1 < 0) {
			return "";
		} else {
			return !this.field5522[var1].isEmpty() ? this.field5525[var1] + " " + this.field5522[var1] : this.field5525[var1];
		}
	}

	@ObfuscatedName("ay")
	@ObfuscatedSignature(
		descriptor = "(I)V",
		garbageValue = "157225242"
	)
	void method10249() {
		this.field5531 = WorldMapLabelSize.fontBold12.stringWidth("Choose Option");

		for (int var1 = 0; var1 < this.field5518; ++var1) {
			int var2 = WorldMapLabelSize.fontBold12.stringWidth(this.method10250(var1));
			if (this.field5526[var1] != null) {
				var2 += 15;
			}

			if (var2 > this.field5531) {
				this.field5531 = var2;
			}
		}

		this.field5531 += 8;
		this.field5532 = this.field5518 * -996409265 + 1165946628;
		if (this.field5524) {
			this.field5532 += -1195691118;
		}

	}

	@ObfuscatedName("an")
	@ObfuscatedSignature(
		descriptor = "(III)V",
		garbageValue = "16777215"
	)
	public final void method10252(int var1, int var2) {
		this.method10249();
		this.field5529 = var1 - this.field5531 / 2;
		if (this.field5529 + this.field5531 > NPC.canvasWidth) {
			this.field5529 = NPC.canvasWidth - this.field5531;
		}

		if (this.field5529 < 0) {
			this.field5529 = 0;
		}

		this.field5530 = var2;
		if (this.field5532 * 1788236865 + this.field5530 > ApproximateRouteStrategy.canvasHeight) {
			this.field5530 = ApproximateRouteStrategy.canvasHeight - this.field5532 * 1788236865;
		}

		if (this.field5530 < 0) {
			this.field5530 = 0;
		}

		if (this.field5533 != -1 && this.field5526[this.field5533] != null) {
			this.field5526[this.field5533].method10253(this);
		}

	}

	@ObfuscatedName("au")
	@ObfuscatedSignature(
		descriptor = "(Lvw;I)V",
		garbageValue = "-1450741089"
	)
	final void method10253(class567 var1) {
		this.method10249();
		this.field5529 = var1.field5529 + var1.field5531;
		if (this.field5531 + this.field5529 > NPC.canvasWidth) {
			this.field5529 = var1.field5529 - this.field5531;
		}

		if (this.field5529 < 0) {
			this.field5529 = 0;
		}

		int var2 = var1.field5518 * 15 - 15 - var1.field5533 * 15 + var1.field5530;
		if (var1.field5524) {
			var2 += 17;
		}

		int var3 = var2 + 19;
		this.field5530 = var2;
		if (this.field5532 * 1788236865 + this.field5530 > ApproximateRouteStrategy.canvasHeight) {
			this.field5530 = var3 - this.field5532 * 1788236865;
		}

		if (this.field5530 < 0) {
			this.field5530 = 0;
		}

	}

	@ObfuscatedName("ax")
	@ObfuscatedSignature(
		descriptor = "(III)I",
		garbageValue = "-1080804360"
	)
	final int method10254(int var1, int var2) {
		for (int var3 = 0; var3 < this.field5518; ++var3) {
			int var4 = this.field5530 + (this.field5518 - 1 - var3) * 15 + 14;
			if (this.field5524) {
				var4 += 17;
			}

			if (var1 > this.field5529 && var1 < this.field5531 + this.field5529 && var2 > var4 - 13 && var2 < var4 + 3) {
				return var3;
			}
		}

		return -1;
	}

	@ObfuscatedName("ao")
	@ObfuscatedSignature(
		descriptor = "(II)V",
		garbageValue = "1126663380"
	)
	public final void method10255(int var1) {
		if (var1 >= 0) {
			GrandExchangeEvents.menuAction(this.field5517[var1], this.field5520[var1], this.field5521[var1], this.field5519[var1], this.field5523[var1], this.field5527[var1], this.field5525[var1], this.field5522[var1], MouseHandler.MouseHandler_lastPressedX, MouseHandler.MouseHandler_lastPressedY);
		}
	}

	@ObfuscatedName("am")
	@ObfuscatedSignature(
		descriptor = "(III)Z",
		garbageValue = "-1481712309"
	)
	public final boolean method10256(int var1, int var2) {
		if (this.field5533 != -1 && this.field5526[this.field5533] != null && this.field5526[this.field5533].method10256(var1, var2)) {
			return true;
		} else if (var1 >= this.field5529 - 10 && var1 <= this.field5531 + this.field5529 + 10 && var2 >= this.field5530 - 10 && var2 <= this.field5532 * 1788236865 + this.field5530 + 10) {
			int var3 = this.method10254(var1, var2);
			if (var3 != -1 && var3 != this.field5533) {
				if (var3 != this.field5534) {
					this.field5534 = var3;
					this.field5535 = RouteStrategy.method5439();
					if (this.field5533 != -1) {
						this.field5535 += 300L;
					}
				}

				if (this.field5535 <= RouteStrategy.method5439()) {
					this.field5534 = -1;
					this.method10260();
					if (this.field5526[var3] != null) {
						this.field5533 = var3;
						this.field5526[var3].method10253(this);
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

	@ObfuscatedName("ac")
	@ObfuscatedSignature(
		descriptor = "(IIB)Z",
		garbageValue = "-106"
	)
	public final boolean method10257(int var1, int var2) {
		if (this.field5533 != -1 && this.field5526[this.field5533] != null && this.field5526[this.field5533].method10257(var1, var2)) {
			return true;
		} else {
			int var3 = this.method10254(var1, var2);
			if (var3 != -1) {
				this.method10255(var3);
				return true;
			} else {
				return false;
			}
		}
	}

	@ObfuscatedName("ae")
	@ObfuscatedSignature(
		descriptor = "(I)V",
		garbageValue = "1096248356"
	)
	public final void method10258() {
		this.field5533 = -1;

		for (int var1 = 0; var1 < this.field5518; ++var1) {
			if (this.field5526[var1] != null) {
				this.field5526[var1].method10258();
			}
		}

	}

	@ObfuscatedName("ad")
	@ObfuscatedSignature(
		descriptor = "(B)V",
		garbageValue = "-73"
	)
	public final void method10259() {
		int var1 = this.field5529;
		int var2 = this.field5530;
		int var3 = this.field5531;
		int var4 = this.field5532 * 1788236865;

		for (int var5 = 0; var5 < Client.rootWidgetCount; ++var5) {
			if (Client.rootWidgetWidths[var5] + Client.rootWidgetXs[var5] > var1 && Client.rootWidgetXs[var5] < var1 + var3 && Client.rootWidgetHeights[var5] + Client.rootWidgetYs[var5] > var2 && Client.rootWidgetYs[var5] < var2 + var4) {
				Client.validRootWidgets[var5] = true;
			}
		}

		this.method10260();
	}

	@ObfuscatedName("aq")
	@ObfuscatedSignature(
		descriptor = "(B)V",
		garbageValue = "0"
	)
	final void method10260() {
		if (this.field5533 != -1) {
			if (this.field5526[this.field5533] != null) {
				this.field5526[this.field5533].method10259();
			}

			this.field5533 = -1;
		}

	}

	@ObfuscatedName("al")
	@ObfuscatedSignature(
		descriptor = "(Ljava/lang/String;Ljava/lang/String;IIIIIZII)I",
		garbageValue = "1855489333"
	)
	public final int method10263(String var1, String var2, int var3, int var4, int var5, int var6, int var7, boolean var8, int var9) {
		if (this.field5518 < this.field5525.length) {
			this.field5525[this.field5518] = var1;
			this.field5522[this.field5518] = var2;
			this.field5521[this.field5518] = var3;
			this.field5519[this.field5518] = var4;
			this.field5517[this.field5518] = var5;
			this.field5520[this.field5518] = var6;
			this.field5523[this.field5518] = var7;
			this.field5527[this.field5518] = var9;
			this.field5528[this.field5518] = var8;
			this.field5526[this.field5518] = null;
			return ++this.field5518 - 1;
		} else {
			return -1;
		}
	}

	@ObfuscatedName("aj")
	@ObfuscatedSignature(
		descriptor = "(I)V",
		garbageValue = "-2114165479"
	)
	public final void method10272() {
		int var1 = this.field5529;
		int var2 = this.field5530;
		int var3 = 6116423;
		Rasterizer2D.Rasterizer2D_fillRectangle(var1, var2, this.field5531, this.field5532 * 1788236865, var3);
		if (this.field5524) {
			Rasterizer2D.Rasterizer2D_fillRectangle(var1 + 1, var2 + 1, this.field5531 - 2, 16, 0);
			Rasterizer2D.Rasterizer2D_drawRectangle(var1 + 1, var2 + 18, this.field5531 - 2, this.field5532 * 1788236865 - 19, 0);
			WorldMapLabelSize.fontBold12.draw("Choose Option", var1 + 3, var2 + 14, var3, -1);
		} else {
			Rasterizer2D.Rasterizer2D_drawRectangle(var1 + 1, var2 + 1, this.field5531 - 2, this.field5532 * 1788236865 - 2, 0);
		}

		int var4 = MouseHandler.MouseHandler_x;
		int var5 = MouseHandler.MouseHandler_y;

		int var6;
		int var7;
		int var8;
		for (var6 = 0; var6 < this.field5518; ++var6) {
			var7 = var2 + (this.field5518 - 1 - var6) * 15 + 14;
			if (this.field5524) {
				var7 += 17;
			}

			var8 = 16777215;
			if (var4 > var1 && var4 < var1 + this.field5531 && var5 > var7 - 13 && var5 < var7 + 3) {
				var8 = 16776960;
			}

			if (var6 == this.field5533 || var6 == this.field5534 && this.field5526[var6] != null) {
				Rasterizer2D.Rasterizer2D_fillRectangle(var1 + 2, var7 - 12, this.field5531 - 4, 15, 7496785);
			}

			WorldMapLabelSize.fontBold12.draw(this.method10250(var6), var1 + 3, var7, var8, 0);
			if (this.field5526[var6] != null) {
				WorldMapLabelSize.fontBold12.method8194(62, var1 + this.field5531 - 10, var7, 16777215);
			}
		}

		var6 = this.field5529;
		var7 = this.field5530;
		var8 = this.field5531;
		int var9 = this.field5532 * 1788236865;

		for (int var10 = 0; var10 < Client.rootWidgetCount; ++var10) {
			if (Client.rootWidgetXs[var10] + Client.rootWidgetWidths[var10] > var6 && Client.rootWidgetXs[var10] < var8 + var6 && Client.rootWidgetHeights[var10] + Client.rootWidgetYs[var10] > var7 && Client.rootWidgetYs[var10] < var7 + var9) {
				Client.field685[var10] = true;
			}
		}

		if (this.field5533 != -1 && this.field5526[this.field5533] != null) {
			this.field5526[this.field5533].method10272();
		}

	}
}

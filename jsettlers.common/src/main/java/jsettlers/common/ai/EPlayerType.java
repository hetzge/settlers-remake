/*******************************************************************************
 * Copyright (c) 2015, 2016
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.common.ai;

/**
 * @author codingberlin
 */
public enum EPlayerType {
	AI_VERY_EASY,
	AI_EASY,
	AI_HARD,
	AI_VERY_HARD,
	HUMAN;

	public static final EPlayerType[] VALUES = EPlayerType.values();
	public static final int NUMBER_OF_PLAYER_TYPES = VALUES.length;

	public static EPlayerType getTypeByIndex(int index) {
		return VALUES[index % NUMBER_OF_PLAYER_TYPES];
	}

	public boolean isAi() {
		return !isHuman();
	}
	
	public boolean isHuman() {
		return HUMAN == this;
	}
}

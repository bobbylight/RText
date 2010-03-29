/*
 * 03/27/2010
 *
 * HtmlOptions.java - Options for tidying HTML.
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This file is a part of RText.
 *
 * RText is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.rtext.plugins.tidy;

import org.fife.ui.app.Prefs;


/**
 * Options for tidying HTML.<p>
 *
 * While all fields in this class are public, please use the getters and
 * setters for accessing them for bounds checking, etc.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class HtmlOptions extends Prefs {

	public int htmlSpaceCount;
	public boolean htmlClean;
	public boolean htmlDropEmptyParas;
	public boolean htmlLogicalEmphasis;
	public boolean hideOptionalEndTags;
	public boolean htmlUpperCaseTagNames;
	public boolean htmlUpperCaseAttrNames;
	public int htmlWrapLength;


	/**
	 * Whether empty "<code>p</code>" elements should be dropped when tidying
	 * HTML.
	 *
	 * @return Whether to drop empty "<code>p</code>" elements.
	 * @see #setDropEmptyParas(boolean)
	 */
	public boolean getDropEmptyParas() {
		return htmlDropEmptyParas;
	}


	/**
	 * Returns whether optional end tags are hidden in HTML.
	 *
	 * @return Whether optional end tags are hidden.
	 * @see #setHideOptionalEndTags(boolean)
	 */
	public boolean getHideOptionalEndTags() {
		return hideOptionalEndTags;
	}


	/**
	 * Returns whether to replace <tt>&lt;i&gt;</tt> and <tt>&lt;b&gt;</tt>
	 * with <tt>&lt;em&gt;</tt> and <tt>&lt;strong&gt;</tt>, respectively.
	 *
	 * @return Whether to use logical emphasis tags.
	 * @see #setLogicalEmphasis(boolean)
	 */
	public boolean getLogicalEmphasis() {
		return htmlLogicalEmphasis;
	}


	/**
	 * Whether presentational tags should be replaced with style rules.
	 *
	 * @return Whether presentational tags should be replaced with style rules.
	 * @see #setMakeClean(boolean)
	 */
	public boolean getMakeClean() {
		return htmlClean;
	}


	/**
	 * Returns the number of spaces to use for indentation when formatting
	 * HTML.
	 *
	 * @return The number of spaces to use.
	 * @see #setSpaceCount(int)
	 */
	public int getSpaceCount() {
		return htmlSpaceCount;
	}


	/**
	 * Returns whether HTML attribute names should be made upper case.
	 *
	 * @return Whether HTML attribute names should be made upper case.
	 * @see #setUpperCaseAttrNames(boolean)
	 */
	public boolean getUpperCaseAttrNames() {
		return htmlUpperCaseAttrNames;
	}


	/**
	 * Returns whether HTML tag names should be made upper case.
	 *
	 * @return Whether HTML tag names should be made upper case.
	 * @see #setUpperCaseTagNames(boolean)
	 */
	public boolean getUpperCaseTagNames() {
		return htmlUpperCaseTagNames;
	}


	/**
	 * Returns how long formatted lines can be before they are wrapped.
	 *
	 * @return The maximum length of a formatted line, or "0" for no limit.
	 * @see #setWrapLength(int)
	 */
	public int getWrapLength() {
		return htmlWrapLength;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setDefaults() {
		htmlSpaceCount = 4;
		htmlClean = false;
		htmlDropEmptyParas = false;
		htmlLogicalEmphasis = false;
		hideOptionalEndTags = false; // Hides "html" and "body" tags (!)
		htmlUpperCaseTagNames = false;
	}


	/**
	 * Sets whether empty "<code>p</code>" elements should be dropped when
	 * tidying HTML.
	 *
	 * @param drop Whether to drop empty "<code>p</code>" elements.
	 * @see #getDropEmptyParas()
	 */
	public void setDropEmptyParas(boolean drop) {
		htmlDropEmptyParas = drop;
	}


	/**
	 * Sets whether optional end tags are hidden in HTML.
	 *
	 * @param hide Whether optional end tags are hidden.
	 * @see #getHideOptionalEndTags()
	 */
	public void setHideOptionalEndTags(boolean hide) {
		hideOptionalEndTags = hide;
	}


	/**
	 * Sets whether to replace <tt>&lt;i&gt;</tt> and <tt>&lt;b&gt;</tt> with
	 * <tt>&lt;em&gt;</tt> and <tt>&lt;strong&gt;</tt>, respectively.
	 *
	 * @param emphasis Whether to use logical emphasis tags.
	 * @see #getLogicalEmphasis()
	 */
	public void setLogicalEmphasis(boolean emphasis) {
		htmlLogicalEmphasis = emphasis;
	}


	/**
	 * Sets whether presentational tags should be replaced with style rules.
	 *
	 * @param clean Whether presentational tags should be replaced with style
	 *        rules.
	 * @see #getMakeClean()
	 */
	public void setMakeClean(boolean clean) {
		htmlClean = clean;
	}


	/**
	 * Sets the number of spaces to use for indentation when formatting HTML.
	 *
	 * @param count The number of spaces.  This should be greater than or
	 *        equal to <tt>-1</tt> (which specifies to use tabs).
	 * @see #getSpaceCount()
	 */
	public void setSpaceCount(int count) {
		htmlSpaceCount = Math.max(-1, count);
	}


	/**
	 * Sets whether HTML attribute names should be made upper-case.
	 *
	 * @param upper Whether they should be made upper case.
	 * @see #getUpperCaseAttrNames()
	 */
	public void setUpperCaseAttrNames(boolean upper) {
		htmlUpperCaseAttrNames = upper;
	}


	/**
	 * Sets whether HTML tag names should be made upper-case.
	 *
	 * @param upper Whether they should be made upper case.
	 * @see #getUpperCaseTagNames()
	 */
	public void setUpperCaseTagNames(boolean upper) {
		htmlUpperCaseTagNames = upper;
	}


	/**
	 * Sets how long formatted lines can be before they are wrapped.
	 *
	 * @param length The maximum length of a formatted line, or "0" for no
	 *        limit.
	 * @see #getWrapLength()
	 */
	public void setWrapLength(int length) {
		htmlWrapLength = Math.max(0, length);
	}


}
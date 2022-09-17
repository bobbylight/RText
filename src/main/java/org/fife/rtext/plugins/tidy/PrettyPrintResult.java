/*
 * 02/15/2013
 *
 * PrettyPrintResult - Result object for a pretty printing operation.
 * Copyright (C) 2013 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tidy;


/**
 * The results of a pretty-print operation.
 *
 * @param result The result of the operation.
 * @param text The pretty-printed text, or {@code null} if it was unsuccessful.
 * @param summary A summary of the operation, or {@code null} if none.
 */
record PrettyPrintResult(int result, String text, String summary) {
}

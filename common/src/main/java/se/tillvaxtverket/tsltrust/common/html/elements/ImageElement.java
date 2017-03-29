/*
 * Copyright 2017 Swedish E-identification Board (E-legitimationsnämnden)
 *  		 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.tillvaxtverket.tsltrust.common.html.elements;

/**
 * HTML Image element
 */
public class ImageElement extends HtmlElement{

    public ImageElement(String src) {
        this(src, 0, "");
    }

    public ImageElement(String src, String alt) {
        this(src, 0, alt);
    }

    public ImageElement(String src, int width) {
        this(src, width, "");
    }
    
    public ImageElement(String src, int width, String alt) {
        this.tag="img";
        addAttribute("src", src);
        if (width!=0){
            this.addAttribute("width", String.valueOf(width));
        }
        if (alt.length()>0){
            this.addAttribute("alt", alt);
        }
    }
        
}

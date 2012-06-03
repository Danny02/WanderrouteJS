/*
 * Copyright (C) 2012 daniel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wanderroutejs.srtmreader;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author daniel
 */
public class JSONGenerator
{
    private final String variableName;

    public JSONGenerator(String variableName)
    {
        this.variableName = variableName;
    }

    public String toJSON(Collection<int[]> strip)
    {
        StringBuilder jsonBuilder = new StringBuilder();

        if (!variableName.isEmpty()) {
            jsonBuilder.append("var ");
            jsonBuilder.append(variableName);
            jsonBuilder.append(" = ");
        }

        if (strip == null || strip.isEmpty()) {
            jsonBuilder.append("[];");
        } else {
            jsonBuilder.append("[");
            jsonBuilder.append("\n");

            for (int[] points : strip) {
                jsonBuilder.append("\t");
                jsonBuilder.append(points[0]);
                jsonBuilder.append(",");
                jsonBuilder.append(points[1]);
                jsonBuilder.append(",");
                jsonBuilder.append(points[2]);
                jsonBuilder.append(",");
                jsonBuilder.append("\n");
            }

            jsonBuilder.append("];");
        }

        return jsonBuilder.toString();
    }
}


package com.baselet.element.facet;

import com.baselet.control.enums.FormatLabels;
import com.baselet.diagram.draw.helper.StyleException;
import com.baselet.gui.AutocompletionText;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KeyValueFacet extends Facet
{
    private Logger log = LoggerFactory.getLogger(KeyValueFacet.class);

    public static final String SEP = "=";

    @Override
    public boolean checkStart(String line, PropertiesParserState state)
    {
        return line.startsWith(getKeyWithSep());
    }

    @Override
    public List<AutocompletionText> getAutocompletionStrings()
    {
        List<AutocompletionText> returnList = new ArrayList<>();

        for (ValueInfo valueInfo : getKeyValue().getValueInfos())
        {
            returnList.add(new AutocompletionText(getKeyWithSep() + valueInfo.getValue().toString().toLowerCase(Locale.ENGLISH), valueInfo.getInfo(), valueInfo.getBase64Img()));
        }

        return returnList;
    }

    public abstract KeyValue getKeyValue();

    public String getKeyWithSep()
    {
        return getKeyValue().getKey() + KeyValueFacet.SEP;
    }

    @Override
    public void handleLine(String line, PropertiesParserState state)
    {
        String value = extractValue(line);
        
        try
        {
            handleValue(value, state);
        } catch (Exception e)
        {
            log.debug("KeyValue Error", e);
            String errorMessage = getKeyValue().getValueString();
            if (e instanceof StyleException)
            { // self defined exceptions overwrite the default message
                errorMessage = e.getMessage();
            }
            throw new RuntimeException(FormatLabels.BOLD.getValue() + "Invalid value:" + FormatLabels.BOLD.getValue() + "\n" + getKeyWithSep() + value + "\n" + errorMessage);
        }
    }

    public abstract void handleValue(String value, PropertiesParserState state);

    protected String extractValue(String line)
    {
        return line.substring(getKeyWithSep().length());
    }

    public static class KeyValue
    {
        private final String key;

        private final boolean allValuesListed;

        private final List<ValueInfo> valueInfos;

        public KeyValue(String key, boolean allValuesListed, String value, String info)
        {
            super();
            this.key = key.toLowerCase(Locale.ENGLISH);
            this.allValuesListed = allValuesListed;
            valueInfos = Arrays.asList(new ValueInfo(value, info));
        }

        public KeyValue(String key, List<ValueInfo> valueInfos)
        {
            super();
            this.key = key;
            allValuesListed = true;
            this.valueInfos = valueInfos;
        }

        public KeyValue(String key, ValueInfo... valueInfos)
        {
            this(key, Arrays.asList(valueInfos));
        }

        public String getKey()
        {
            return key;
        }

        public List<ValueInfo> getValueInfos()
        {
            return Collections.unmodifiableList(valueInfos);
        }

        public String getValueString()
        {
            StringBuilder sb = new StringBuilder();
            if (allValuesListed)
            {
                sb.append("Valid are: ");
                for (ValueInfo vi : valueInfos)
                {
                    sb.append(vi.value.toString().toLowerCase(Locale.ENGLISH)).append(',');
                }
                sb.deleteCharAt(sb.length() - 1);
            } else
            {
                for (ValueInfo vi : valueInfos)
                {
                    sb.append(vi.info);
                }
            }
            return sb.toString();
        }
    }

    public static class ValueInfo
    {
        private final Object value;

        private final String info;

        private final String base64Img;

        public ValueInfo(Object value, String info)
        {
            this(value, info, null);
        }

        public ValueInfo(Object value, String info, String base64Img)
        {
            super();
            this.value = value;
            this.info = info;
            this.base64Img = base64Img;
        }

        public Object getValue()
        {
            return value;
        }

        private String getInfo()
        {
            return info;
        }

        private String getBase64Img()
        {
            return base64Img;
        }
    }
}

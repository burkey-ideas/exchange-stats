package au.com.burkey.exchangestats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.json.JsonObject;

import com.opencsv.CSVWriter;

public class CurrencyApiJob implements Runnable
{
    private static final Map<String, String> currencyMap = new TreeMap<String, String>();

    @Override
    public void run()
    {
        System.out.println("Running: " + this.getClass().getSimpleName());

        final String currencyListApiUrl = "http://apilayer.net/api/list?access_key=%s";
        final String exchangeRateApiUrl = "http://apilayer.net/api/live?access_key=%s&currencies=%s";

        //String exampleCurrencyListResult = "{\"success\":true,\"terms\":\"https://currencylayer.com/terms\",\"privacy\":\"https://currencylayer.com/privacy\",\"currencies\":{\"AED\":\"United Arab Emirates Dirham\",\"AFN\":\"Afghan Afghani\",\"ALL\":\"Albanian Lek\",\"AMD\":\"Armenian Dram\",\"ANG\":\"Netherlands Antillean Guilder\",\"AOA\":\"Angolan Kwanza\",\"ARS\":\"Argentine Peso\",\"AUD\":\"Australian Dollar\",\"AWG\":\"Aruban Florin\",\"AZN\":\"Azerbaijani Manat\",\"BAM\":\"Bosnia-Herzegovina Convertible Mark\",\"BBD\":\"Barbadian Dollar\",\"BDT\":\"Bangladeshi Taka\",\"BGN\":\"Bulgarian Lev\",\"BHD\":\"Bahraini Dinar\",\"BIF\":\"Burundian Franc\",\"BMD\":\"Bermudan Dollar\",\"BND\":\"Brunei Dollar\",\"BOB\":\"Bolivian Boliviano\",\"BRL\":\"Brazilian Real\",\"BSD\":\"Bahamian Dollar\",\"BTC\":\"Bitcoin\",\"BTN\":\"Bhutanese Ngultrum\",\"BWP\":\"Botswanan Pula\",\"BYN\":\"New Belarusian Ruble\",\"BYR\":\"Belarusian Ruble\",\"BZD\":\"Belize Dollar\",\"CAD\":\"Canadian Dollar\",\"CDF\":\"Congolese Franc\",\"CHF\":\"Swiss Franc\",\"CLF\":\"Chilean Unit of Account (UF)\",\"CLP\":\"Chilean Peso\",\"CNY\":\"Chinese Yuan\",\"COP\":\"Colombian Peso\",\"CRC\":\"Costa Rican Colón\",\"CUC\":\"Cuban Convertible Peso\",\"CUP\":\"Cuban Peso\",\"CVE\":\"Cape Verdean Escudo\",\"CZK\":\"Czech Republic Koruna\",\"DJF\":\"Djiboutian Franc\",\"DKK\":\"Danish Krone\",\"DOP\":\"Dominican Peso\",\"DZD\":\"Algerian Dinar\",\"EGP\":\"Egyptian Pound\",\"ERN\":\"Eritrean Nakfa\",\"ETB\":\"Ethiopian Birr\",\"EUR\":\"Euro\",\"FJD\":\"Fijian Dollar\",\"FKP\":\"Falkland Islands Pound\",\"GBP\":\"British Pound Sterling\",\"GEL\":\"Georgian Lari\",\"GGP\":\"Guernsey Pound\",\"GHS\":\"Ghanaian Cedi\",\"GIP\":\"Gibraltar Pound\",\"GMD\":\"Gambian Dalasi\",\"GNF\":\"Guinean Franc\",\"GTQ\":\"Guatemalan Quetzal\",\"GYD\":\"Guyanaese Dollar\",\"HKD\":\"Hong Kong Dollar\",\"HNL\":\"Honduran Lempira\",\"HRK\":\"Croatian Kuna\",\"HTG\":\"Haitian Gourde\",\"HUF\":\"Hungarian Forint\",\"IDR\":\"Indonesian Rupiah\",\"ILS\":\"Israeli New Sheqel\",\"IMP\":\"Manx pound\",\"INR\":\"Indian Rupee\",\"IQD\":\"Iraqi Dinar\",\"IRR\":\"Iranian Rial\",\"ISK\":\"Icelandic Króna\",\"JEP\":\"Jersey Pound\",\"JMD\":\"Jamaican Dollar\",\"JOD\":\"Jordanian Dinar\",\"JPY\":\"Japanese Yen\",\"KES\":\"Kenyan Shilling\",\"KGS\":\"Kyrgystani Som\",\"KHR\":\"Cambodian Riel\",\"KMF\":\"Comorian Franc\",\"KPW\":\"North Korean Won\",\"KRW\":\"South Korean Won\",\"KWD\":\"Kuwaiti Dinar\",\"KYD\":\"Cayman Islands Dollar\",\"KZT\":\"Kazakhstani Tenge\",\"LAK\":\"Laotian Kip\",\"LBP\":\"Lebanese Pound\",\"LKR\":\"Sri Lankan Rupee\",\"LRD\":\"Liberian Dollar\",\"LSL\":\"Lesotho Loti\",\"LTL\":\"Lithuanian Litas\",\"LVL\":\"Latvian Lats\",\"LYD\":\"Libyan Dinar\",\"MAD\":\"Moroccan Dirham\",\"MDL\":\"Moldovan Leu\",\"MGA\":\"Malagasy Ariary\",\"MKD\":\"Macedonian Denar\",\"MMK\":\"Myanma Kyat\",\"MNT\":\"Mongolian Tugrik\",\"MOP\":\"Macanese Pataca\",\"MRO\":\"Mauritanian Ouguiya\",\"MUR\":\"Mauritian Rupee\",\"MVR\":\"Maldivian Rufiyaa\",\"MWK\":\"Malawian Kwacha\",\"MXN\":\"Mexican Peso\",\"MYR\":\"Malaysian Ringgit\",\"MZN\":\"Mozambican Metical\",\"NAD\":\"Namibian Dollar\",\"NGN\":\"Nigerian Naira\",\"NIO\":\"Nicaraguan Córdoba\",\"NOK\":\"Norwegian Krone\",\"NPR\":\"Nepalese Rupee\",\"NZD\":\"New Zealand Dollar\",\"OMR\":\"Omani Rial\",\"PAB\":\"Panamanian Balboa\",\"PEN\":\"Peruvian Nuevo Sol\",\"PGK\":\"Papua New Guinean Kina\",\"PHP\":\"Philippine Peso\",\"PKR\":\"Pakistani Rupee\",\"PLN\":\"Polish Zloty\",\"PYG\":\"Paraguayan Guarani\",\"QAR\":\"Qatari Rial\",\"RON\":\"Romanian Leu\",\"RSD\":\"Serbian Dinar\",\"RUB\":\"Russian Ruble\",\"RWF\":\"Rwandan Franc\",\"SAR\":\"Saudi Riyal\",\"SBD\":\"Solomon Islands Dollar\",\"SCR\":\"Seychellois Rupee\",\"SDG\":\"Sudanese Pound\",\"SEK\":\"Swedish Krona\",\"SGD\":\"Singapore Dollar\",\"SHP\":\"Saint Helena Pound\",\"SLL\":\"Sierra Leonean Leone\",\"SOS\":\"Somali Shilling\",\"SRD\":\"Surinamese Dollar\",\"STD\":\"São Tomé and Príncipe Dobra\",\"SVC\":\"Salvadoran Colón\",\"SYP\":\"Syrian Pound\",\"SZL\":\"Swazi Lilangeni\",\"THB\":\"Thai Baht\",\"TJS\":\"Tajikistani Somoni\",\"TMT\":\"Turkmenistani Manat\",\"TND\":\"Tunisian Dinar\",\"TOP\":\"Tongan Paʻanga\",\"TRY\":\"Turkish Lira\",\"TTD\":\"Trinidad and Tobago Dollar\",\"TWD\":\"New Taiwan Dollar\",\"TZS\":\"Tanzanian Shilling\",\"UAH\":\"Ukrainian Hryvnia\",\"UGX\":\"Ugandan Shilling\",\"USD\":\"United States Dollar\",\"UYU\":\"Uruguayan Peso\",\"UZS\":\"Uzbekistan Som\",\"VEF\":\"Venezuelan Bolívar Fuerte\",\"VND\":\"Vietnamese Dong\",\"VUV\":\"Vanuatu Vatu\",\"WST\":\"Samoan Tala\",\"XAF\":\"CFA Franc BEAC\",\"XAG\":\"Silver (troy ounce)\",\"XAU\":\"Gold (troy ounce)\",\"XCD\":\"East Caribbean Dollar\",\"XDR\":\"Special Drawing Rights\",\"XOF\":\"CFA Franc BCEAO\",\"XPF\":\"CFP Franc\",\"YER\":\"Yemeni Rial\",\"ZAR\":\"South African Rand\",\"ZMK\":\"Zambian Kwacha (pre-2013)\",\"ZMW\":\"Zambian Kwacha\",\"ZWL\":\"Zimbabwean Dollar\"}}";
        //String exampleExchangeRateResult = "{\"success\":true,\"terms\":\"https://currencylayer.com/terms\",\"privacy\":\"https://currencylayer.com/privacy\",\"timestamp\":1516947254,\"source\":\"USD\",\"quotes\":{\"USDFJD\":1.982503,\"USDMXN\":18.567801,\"USDSTD\":19705.599609,\"USDLVL\":0.62055,\"USDSCR\":13.697019,\"USDCDF\":1565.499517,\"USDBBD\":2,\"USDGTQ\":7.336007,\"USDCLP\":599.780029,\"USDHNL\":23.534979,\"USDUGX\":3624.99998,\"USDZAR\":11.899898,\"USDTND\":2.418303,\"USDCUC\":1,\"USDBSD\":1,\"USDSLL\":7630.000538,\"USDSDG\":7.000197,\"USDIQD\":1184,\"USDCUP\":26.5,\"USDGMD\":48.41034,\"USDTWD\":29.103001,\"USDRSD\":95.082985,\"USDDOP\":47.779999,\"USDKMF\":411.049988,\"USDMYR\":3.874034,\"USDFKP\":0.7047,\"USDXOF\":523.960022,\"USDGEL\":2.489897,\"USDBTC\":0.000088,\"USDUYU\":28.309999,\"USDMAD\":9.135202,\"USDCVE\":88.680145,\"USDTOP\":2.223205,\"USDAZN\":1.6996,\"USDOMR\":0.384802,\"USDPGK\":3.154997,\"USDKES\":102.129997,\"USDSEK\":7.90113,\"USDBTN\":63.599998,\"USDUAH\":28.709999,\"USDGNF\":9000.000155,\"USDERN\":15.289811,\"USDMZN\":59.200001,\"USDSVC\":8.750125,\"USDARS\":19.552033,\"USDQAR\":3.641303,\"USDIRR\":36775.000156,\"USDMRO\":351.999894,\"USDCNY\":6.331897,\"USDTHB\":31.360001,\"USDUZS\":8141.000555,\"USDXPF\":96.119033,\"USDBDT\":82.870003,\"USDLYD\":1.331976,\"USDBMD\":1,\"USDKWD\":0.299801,\"USDPHP\":50.900002,\"USDRUB\":55.895199,\"USDPYG\":5599.000099,\"USDISK\":100.199997,\"USDJMD\":123.629997,\"USDCOP\":2788.300049,\"USDMKD\":49.240002,\"USDUSD\":1,\"USDDZD\":113.083969,\"USDPAB\":1,\"USDGGP\":0.704875,\"USDSGD\":1.30795,\"USDETB\":27.209999,\"USDJEP\":0.704875,\"USDKGS\":68.370003,\"USDSOS\":563.000049,\"USDVEF\":9.975009,\"USDVUV\":102.749604,\"USDLAK\":8274.000203,\"USDBND\":1.310602,\"USDZMK\":9001.199721,\"USDXAF\":527.109985,\"USDLRD\":127.870003,\"USDXAG\":0.057335,\"USDCHF\":0.938811,\"USDHRK\":5.966303,\"USDALL\":106.699997,\"USDDJF\":176.830002,\"USDZMW\":9.680412,\"USDTZS\":2240.999954,\"USDVND\":22709,\"USDXAU\":0.000739,\"USDAUD\":1.241301,\"USDILS\":3.392012,\"USDGHS\":4.518501,\"USDGYD\":205.490005,\"USDKPW\":899.999983,\"USDBOB\":6.859924,\"USDKHR\":4017.999761,\"USDMDL\":16.655001,\"USDIDR\":13308,\"USDKYD\":0.819957,\"USDAMD\":480.149994,\"USDBWP\":9.546098,\"USDSHP\":0.70504,\"USDTRY\":3.7535,\"USDLBP\":1505.400024,\"USDTJS\":8.808798,\"USDJOD\":0.707505,\"USDAED\":3.672299,\"USDHKD\":7.818098,\"USDRWF\":835.75,\"USDEUR\":0.803996,\"USDLSL\":11.809937,\"USDDKK\":5.98623,\"USDCAD\":1.23456,\"USDBGN\":1.576397,\"USDMMK\":1320.999895,\"USDMUR\":32.099998,\"USDNOK\":7.71406,\"USDSYP\":514.97998,\"USDIMP\":0.704875,\"USDZWL\":322.355011,\"USDGIP\":0.705026,\"USDRON\":3.754802,\"USDLKR\":153.699997,\"USDNGN\":358.999728,\"USDCRC\":564.599976,\"USDCZK\":20.381599,\"USDPKR\":110.459999,\"USDXCD\":2.692896,\"USDANG\":1.779702,\"USDHTG\":63.599998,\"USDBHD\":0.376697,\"USDKZT\":319.959991,\"USDSRD\":7.396152,\"USDSZL\":11.890344,\"USDLTL\":3.048699,\"USDSAR\":3.749906,\"USDTTD\":6.721497,\"USDYER\":249.899994,\"USDMVR\":15.570061,\"USDAFN\":69.110001,\"USDINR\":63.544498,\"USDAWG\":1.78,\"USDKRW\":1063.609985,\"USDNPR\":101.720001,\"USDJPY\":109.287003,\"USDMNT\":2416.000315,\"USDAOA\":202.781998,\"USDPLN\":3.330599,\"USDGBP\":0.70487,\"USDSBD\":7.7046,\"USDBYN\":2.019912,\"USDHUF\":248.360001,\"USDBYR\":19600,\"USDBIF\":1750.97998,\"USDMWK\":717.999877,\"USDMGA\":3190.999718,\"USDXDR\":0.687423,\"USDBZD\":1.997801,\"USDBAM\":1.575297,\"USDEGP\":17.639999,\"USDMOP\":8.047199,\"USDNAD\":11.888975,\"USDNIO\":30.897595,\"USDPEN\":3.2098,\"USDNZD\":1.362502,\"USDWST\":2.503104,\"USDTMT\":3.4,\"USDCLF\":0.02215,\"USDBRL\":3.146802}}";

        try
        {
            String accessKey = PropertyUtil.getProperties().getProperty("currency.accessKey");
            String currencyFile = PropertyUtil.getProperties().getProperty("currency.file");

            if (currencyMap.isEmpty())
            {
                System.out.println("Retrieving currency list.");

                //JsonObject currencyListResult = Json.createReader(new StringReader(exampleCurrencyListResult)).readObject();
                JsonObject currencyListResult = UrlUtil.getJsonUrl(currencyListApiUrl, accessKey);
                checkResult(currencyListResult);

                JsonObject currencyList = currencyListResult.getJsonObject("currencies");

                for(String currencyKey : currencyList.keySet())
                {
                    currencyMap.put(currencyKey, currencyList.getString(currencyKey));
                }

                System.out.println("Currency list size: " + currencyMap.size());

                System.out.println("Currency list: " + UrlUtil.toStringKeys(currencyMap));
            }

            System.out.println("Retrieving exchange rates.");

            //JsonObject exchangeRateResult = Json.createReader(new StringReader(exampleExchangeRateResult)).readObject();
            JsonObject exchangeRateResult = UrlUtil.getJsonUrl(exchangeRateApiUrl, accessKey, UrlUtil.toStringKeys(currencyMap));
            checkResult(exchangeRateResult);

            String sourceCurrency = exchangeRateResult.getString("source");
            Date timestamp = new Date(exchangeRateResult.getJsonNumber("timestamp").longValue() * 1000L);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = format.format(timestamp);

            System.out.println("Exchnage Rate date: " + formattedDate);
            System.out.println("Exchange Rate source currency: " + sourceCurrency);

            JsonObject exchangeRateList = exchangeRateResult.getJsonObject("quotes");

            Map<String, String> exchangeRateMap = new TreeMap<String, String>();

            for(String exchangeRateKey : exchangeRateList.keySet())
            {
                int index = exchangeRateKey.indexOf(sourceCurrency);

                if (index > -1)
                {
                    exchangeRateMap.put(
                            exchangeRateKey.substring(index + sourceCurrency.length()),
                            String.valueOf(exchangeRateList.getJsonNumber(exchangeRateKey).bigDecimalValue()));
                }
                else
                {
                    exchangeRateMap.put(
                            exchangeRateKey,
                            String.valueOf(exchangeRateList.getJsonNumber(exchangeRateKey).bigDecimalValue()));
                }
            }

            System.out.println("Exchnage Rate list size: " + exchangeRateMap.size());

            boolean append = new File(currencyFile).exists();
            try (CSVWriter writer = new CSVWriter(new FileWriter(currencyFile, append)))
            {
                if (append)
                {
                    System.out.println("Previous currency file detected, not writing header row.");
                }
                else
                {
                    System.out.println("Previous currency file not detected, writing header row.");

                    List<String> header = new ArrayList<String>();
                    header.add("Date");
                    header.add("Source");
                    header.addAll(exchangeRateMap.keySet());

                    writer.writeNext(header.toArray(new String[header.size()]));
                }

                System.out.println("Writing data row.");

                List<String> data = new ArrayList<String>();
                data.add(formattedDate);
                data.add(sourceCurrency);
                data.addAll(exchangeRateMap.values());

                writer.writeNext(data.toArray(new String[data.size()]));
            }

            System.out.println("Currency file written.");
        }
        catch (WebException ex)
        {
            System.err.println("Unable to examine Currency. " + ex.getMessage());
            System.err.println("Response Code: " + ex.getResponseCode());
            System.err.println("Response Text: " + ex.getResponseText());
        }
        catch (IOException ex)
        {
            System.err.println("Unable to examine Currency. " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean checkResult(final JsonObject result) throws WebException
    {
        if (result.getBoolean("success"))
        {
            return true;
        }
        else
        {
            JsonObject error = result.getJsonObject("error");
            throw new WebException(error.getInt("code"), error.getString("info"), "Currency Api Failure.", null);
        }
    }
}

package test.expressions.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.records.Date;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import test.expressions.Colour;


@ImplementedBy(Conversions.ConversionsDefault.class)
public abstract class Conversions implements RosettaFunction {

    /**
     * @param s
     * @param colour
     * @param d
     * @param dt
     * @param zdt
     * @return result
     */
    public Date evaluate(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt) {
        Date result = doEvaluate(s, colour, d, dt, zdt);
        return result;
    }

    protected abstract Date doEvaluate(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt);

    protected abstract MapperC<BigDecimal> numbers(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt);

    protected abstract MapperC<String> enumConversions(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt);

    protected abstract MapperC<String> temporals(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt);

    protected abstract MapperC<Integer> recordFeatures(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt);

    protected abstract MapperC<LocalTime> times(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt);

    protected abstract MapperS<String> timezone(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt);

    protected abstract MapperS<LocalDateTime> dateTimes(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt);

    protected abstract MapperS<ZonedDateTime> zonedDateTimes(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt);

    public static class ConversionsDefault extends Conversions {
        @Override
        protected Date doEvaluate(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt) {
            Date result = null;
            return assignOutput(result, s, colour, d, dt, zdt);
        }

        protected Date assignOutput(Date result, String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt) {
            result = Date.of(2024, 1, 2);
            final Integer year = MapperS.of(d).<Integer>map("Year", Date::getYear).get();
            if (year != null) {
                result = Date.of(year, 1, 2);
            } else {
                result = null;
            }
            return result;
        }

        @Override
        protected MapperC<BigDecimal> numbers(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt) {
            return MapperC.<BigDecimal>of(MapperS.of(s).checkedMap("to-number", BigDecimal::new, NumberFormatException.class), MapperS.of(s).checkedMap("to-int", Integer::parseInt, NumberFormatException.class).<BigDecimal>map("Type coercion", integer -> integer == null ? null : BigDecimal.valueOf(integer)));
        }

        @Override
        protected MapperC<String> enumConversions(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt) {
            return MapperC.<String>of(MapperS.of(s).checkedMap("to-enum", Colour::fromDisplayName, IllegalArgumentException.class).checkedMap("to-enum", e -> Colour.valueOf(e.name()), IllegalArgumentException.class).map("to-string", Colour::toDisplayString), MapperS.of(colour).map("to-string", Colour::toDisplayString), MapperS.of(1).map("to-string", Object::toString));
        }

        @Override
        protected MapperC<String> temporals(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt) {
            return MapperC.<String>of(MapperS.of(s).checkedMap("to-date", Date::parse, DateTimeParseException.class).map("to-string", Object::toString), MapperS.of(s).checkedMap("to-time", _s -> LocalTime.parse(_s, DateTimeFormatter.ISO_LOCAL_TIME), DateTimeParseException.class).map("to-string", Object::toString), MapperS.of(s).checkedMap("to-date-time", LocalDateTime::parse, DateTimeParseException.class).map("to-string", Object::toString), MapperS.of(s).checkedMap("to-zoned-date-time", ZonedDateTime::parse, DateTimeParseException.class).map("to-string", Object::toString));
        }

        @Override
        protected MapperC<Integer> recordFeatures(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt) {
            return MapperC.<Integer>of(MapperS.of(d).<Integer>map("Day", Date::getDay), MapperS.of(d).<Integer>map("Month", Date::getMonth), MapperS.of(d).<Integer>map("Year", Date::getYear), MapperS.of(dt).<Date>map("Date", _dt -> Date.of(_dt.toLocalDate())).<Integer>map("Day", Date::getDay), MapperS.of(zdt).<Date>map("Date", _zdt -> Date.of(_zdt.toLocalDate())).<Integer>map("Day", Date::getDay));
        }

        @Override
        protected MapperC<LocalTime> times(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt) {
            return MapperC.<LocalTime>of(MapperS.of(dt).<LocalTime>map("Time", LocalDateTime::toLocalTime), MapperS.of(zdt).<LocalTime>map("Time", ZonedDateTime::toLocalTime));
        }

        @Override
        protected MapperS<String> timezone(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt) {
            return MapperS.of(zdt).<String>map("Timezone", _zdt -> _zdt.getZone().getId());
        }

        @Override
        protected MapperS<LocalDateTime> dateTimes(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt) {
            final LocalTime time = MapperS.of(dt).<LocalTime>map("Time", LocalDateTime::toLocalTime).get();
            return d != null && time != null ? MapperS.of(LocalDateTime.of(d.toLocalDate(), time)) : MapperS.<LocalDateTime>ofNull();
        }

        @Override
        protected MapperS<ZonedDateTime> zonedDateTimes(String s, Colour colour, Date d, LocalDateTime dt, ZonedDateTime zdt) {
            final LocalTime time = MapperS.of(dt).<LocalTime>map("Time", LocalDateTime::toLocalTime).get();
            final String _timezone = "UTC";
            return d != null && time != null && _timezone != null ? MapperS.of(ZonedDateTime.of(d.toLocalDate(), time, ZoneId.of(_timezone))) : MapperS.<ZonedDateTime>ofNull();
        }
    }
}

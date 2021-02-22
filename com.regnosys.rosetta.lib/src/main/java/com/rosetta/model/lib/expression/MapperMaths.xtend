package com.rosetta.model.lib.expression

import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime

import static java.time.temporal.ChronoUnit.DAYS
import com.rosetta.model.lib.mapper.Mapper
import com.rosetta.model.lib.mapper.MapperS

class MapperMaths {
	def static <R, A, B> Mapper<R> add(Mapper<A> in1, Mapper<B> in2) {
		if (in1.resultCount()==1 && in2.resultCount()==1) {
			val arg1 = in1.get()
			val arg2 = in2.get()
			return MapperS.of((arg1 + arg2) as R)
		}
		return null;
	}
	
	def static <R, A, B> Mapper<R> subtract(Mapper<A> in1, Mapper<B> in2) {
		if (in1.resultCount()==1 && in2.resultCount()==1) {
			val arg1 = in1.get()
			val arg2 = in2.get()
			return MapperS.of((arg1 - arg2) as R)
		}
		return null;
	}
	def static <R, A, B> Mapper<R> multiply(Mapper<A> in1, Mapper<B> in2) {
		if (in1.resultCount()==1 && in2.resultCount()==1) {
			val arg1 = in1.get()
			val arg2 = in2.get()
			return MapperS.of((arg1 * arg2) as R)
		}
		return null;
	}
	
	def static <R, A, B> Mapper<R> divide(Mapper<A> in1, Mapper<B> in2) {
		if (in1.resultCount()==1 && in2.resultCount()==1) {
			val arg1 = in1.get()
			val arg2 = in2.get()
			return MapperS.of((arg1 / arg2) as R)
		}
		return null;
	}
	
	// Plus
	private static def dispatch Object operator_plus(Object a, Object b) {
		throw new RuntimeException('''Cant add two random («a.class.simpleName», «b.class.simpleName») together''')
	}
	
	private static def dispatch BigDecimal operator_plus(LocalDate d1, LocalDate d2) {
		throw new RuntimeException('''Cant add two dates together''')
	}
	
	private static def dispatch LocalDateTime operator_plus(LocalDate d, LocalTime t) {
		LocalDateTime.of(d,t)
	}
	
	private static def dispatch String operator_plus(String a, String b) {
		return a + b
	}
	
	private static def dispatch Integer operator_plus(Integer a, Integer b) {
		return a.intValue + b.intValue;
	}
	
	private static def dispatch BigDecimal operator_plus(Number a, Number b) {
		val bigA = toBigD(a);
		val bigB = toBigD(b);
		return bigA + bigB;
	}
	
	// Minus
	private static def dispatch Object operator_minus(Object a, Object b) {
		throw new RuntimeException('''Cant subtract two random («a.class.simpleName», «b.class.simpleName») together''')
	}
	
	private static def dispatch BigDecimal operator_minus(LocalDate d1, LocalDate d2) {
		BigDecimal.valueOf(DAYS.between(d2, d1))
	}
	
	private static def dispatch LocalDateTime operator_minus(LocalDate d, LocalTime t) {
		throw new RuntimeException('''Cant subtract time from date''')
	}
	
	private static def dispatch String operator_minus(String a, String b) {
		throw new RuntimeException('''Cant subtract two strings together''')
	}
	
	private static def dispatch Integer operator_minus(Integer a, Integer b) {
		return a.intValue - b.intValue;
	}
	
	private static def dispatch BigDecimal operator_minus(Number a, Number b) {
		val bigA = toBigD(a);
		val bigB = toBigD(b);
		return bigA - bigB;
	}

	private static def dispatch Object *(Object a, Object b) {
		throw new RuntimeException('''Cant multiply two random («a.class.simpleName», «b.class.simpleName») together''')
	}
	
	private static def dispatch BigDecimal *(LocalDate d1, LocalDate d2) {
		throw new RuntimeException('''Cant multiply date and date''')
	}
	
	private static def dispatch LocalDateTime *(LocalDate d, LocalTime t) {
		throw new RuntimeException('''Cant multiply time and date''')
	}
	
	private static def dispatch String * (String a, String b) {
		throw new RuntimeException('''Cant multiply two strings together''')
	}
	
	private static def dispatch Integer * (Integer a, Integer b) {
		return a.intValue * b.intValue;
	}
	
	private static def dispatch BigDecimal * (Number a, Number b) {
		val bigA = toBigD(a);
		val bigB = toBigD(b);
		return bigA * bigB;
	}
	
	private static def dispatch Object /(Object a, Object b) {
		throw new RuntimeException('''Cant divide two random («a.class.simpleName», «b.class.simpleName»)''')
	}
	
	private static def dispatch BigDecimal /(LocalDate d1, LocalDate d2) {
		throw new RuntimeException('''Cant divide date and date''')
	}
	
	private static def dispatch LocalDateTime /(LocalDate d, LocalTime t) {
		throw new RuntimeException('''Cant divide time and date''')
	}
	
	private static def dispatch String / (String a, String b) {
		throw new RuntimeException('''Cant divide two strings''')
	}
	
	private static def dispatch BigDecimal / (Integer a, Integer b) {
		val bigA = toBigD(a);
		val bigB = toBigD(b);
		return bigA / bigB;
	}
	
	private static def dispatch BigDecimal / (Number a, Number b) {
		val bigA = toBigD(a);
		val bigB = toBigD(b);
		return bigA / bigB;
	}
	
	def static BigDecimal toBigD(Number n) {
		switch (n) {
			BigDecimal: return n
			Long: return new BigDecimal(n.longValue())
			Integer: return new BigDecimal(n.intValue())
		}
	}
}
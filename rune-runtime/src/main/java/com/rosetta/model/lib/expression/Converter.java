/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rosetta.model.lib.expression;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.function.Function;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class Converter {
	private static final Table<Class<?>, Class<?>, Function<?,?>> convertors = HashBasedTable.create();
	
	static {
		insert(String.class, Double.class, s->Double.valueOf(s));
		insert(String.class, Float.class, s->Float.valueOf(s));
		insert(String.class, Integer.class, s->Integer.valueOf(s));
		insert(String.class, LocalTime.class, s->LocalTime.parse(s));
		insert(String.class, LocalDate.class, s->LocalDate.parse(s));
		insert(String.class, LocalDateTime.class, s->LocalDateTime.parse(s));
		insert(String.class, ZonedDateTime.class, s->ZonedDateTime.parse(s));
		insert(String.class, BigDecimal.class, s->new BigDecimal(s));
		insert(String.class, Boolean.class, s->Boolean.valueOf(s));
		
		insert(Double.class, String.class, d->Double.toString(d));
		insert(Double.class, Float.class, d->d.floatValue());
		insert(Double.class, Integer.class, d->d.intValue());
		insert(Double.class, BigDecimal.class, d->new BigDecimal(d));
		
		insert(Float.class, String.class, f->Float.toString(f));
		insert(Float.class, Double.class, f->f.doubleValue());
		insert(Float.class, Integer.class, f->f.intValue());
		insert(Float.class, BigDecimal.class, f->new BigDecimal(f));
		
		insert(Integer.class, String.class, i->Integer.toString(i));
		insert(Integer.class, Double.class, i->i.doubleValue());
		insert(Integer.class, Float.class, i->i.floatValue());
		insert(Integer.class, BigDecimal.class, i->new BigDecimal(i));
		
		insert(LocalTime.class, String.class, lt->lt.toString());
		insert(LocalDate.class, String.class, ld->ld.toString());
		insert(LocalDate.class, LocalDateTime.class, ld->ld.atStartOfDay());
		insert(LocalDateTime.class, String.class, ldt->ldt.toString());
		insert(LocalDateTime.class, LocalTime.class, ldt->ldt.toLocalTime());
		insert(LocalDateTime.class, LocalDate.class, ldt->ldt.toLocalDate());
		
		insert(ZonedDateTime.class, String.class, ldt->ldt.toString());
		
		insert(BigDecimal.class, String.class, bd->bd.toString());
		insert(BigDecimal.class, Double.class, bd->bd.doubleValue());
		insert(BigDecimal.class, Float.class, bd->bd.floatValue());
		insert(BigDecimal.class, Integer.class, bd->bd.intValue());
		
		insert(Boolean.class, String.class, b->Boolean.toString(b));
	}
	
	private static <A,B> void insert(Class<A> a, Class<B> b, Function<A, B> func) {
		convertors.put(a, b, func);
	}
	

	@SuppressWarnings("unchecked")
	public static <C, A> C convert(Class<C> clazz, A t) {
		if (t==null) return null;
		Class<?> classa = t.getClass();
		if (classa.equals(clazz)) return (C)t;
		Function<A,C> func= (Function<A, C>) convertors.get(classa, clazz);
		if (func!=null) {
			return func.apply(t);
		}
		return null;//should be an issue?
	}
}

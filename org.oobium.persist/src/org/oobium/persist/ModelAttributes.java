/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.persist;

import java.lang.annotation.Annotation;
import java.util.Date;

public class ModelAttributes {

	public static final Attribute createdAt = new Attribute() {
		@Override
		public Class<? extends Annotation> annotationType() {
			return Attribute.class;
		}
		
		@Override
		public boolean virtual() {
			return false;
		}
		
		@Override
		public boolean unique() {
			return false;
		}
		
		@Override
		public Class<?> type() {
			return Date.class;
		}
		
		@Override
		public int scale() {
			return 0;
		}

		@Override
		public boolean readOnly() {
			return false;
		}
		
		@Override
		public int precision() {
			return 0;
		}
		
		@Override
		public String name() {
			return "createdAt";
		}
		
		@Override
		public String init() {
			return "";
		}
		
		@Override
		public boolean indexed() {
			return false;
		}
		
		@Override
		public String check() {
			return "";
		}
	};

	public static final Attribute createdOn = new Attribute() {
		@Override
		public Class<? extends Annotation> annotationType() {
			return Attribute.class;
		}
		
		@Override
		public boolean virtual() {
			return false;
		}
		
		@Override
		public boolean unique() {
			return false;
		}
		
		@Override
		public Class<?> type() {
			return Date.class;
		}
		
		@Override
		public int scale() {
			return 0;
		}

		@Override
		public boolean readOnly() {
			return false;
		}
		
		@Override
		public int precision() {
			return 0;
		}
		
		@Override
		public String name() {
			return "createdOn";
		}
		
		@Override
		public String init() {
			return "";
		}
		
		@Override
		public boolean indexed() {
			return false;
		}
		
		@Override
		public String check() {
			return "";
		}
	};
	
	public static final Attribute updatedAt = new Attribute() {
		@Override
		public Class<? extends Annotation> annotationType() {
			return Attribute.class;
		}
		
		@Override
		public boolean virtual() {
			return false;
		}
		
		@Override
		public boolean unique() {
			return false;
		}
		
		@Override
		public Class<?> type() {
			return Date.class;
		}
		
		@Override
		public int scale() {
			return 0;
		}

		@Override
		public boolean readOnly() {
			return false;
		}
		
		@Override
		public int precision() {
			return 0;
		}
		
		@Override
		public String name() {
			return "updatedAt";
		}
		
		@Override
		public String init() {
			return "";
		}
		
		@Override
		public boolean indexed() {
			return false;
		}
		
		@Override
		public String check() {
			return "";
		}
	};

	public static final Attribute updatedOn = new Attribute() {
		@Override
		public Class<? extends Annotation> annotationType() {
			return Attribute.class;
		}
		
		@Override
		public boolean virtual() {
			return false;
		}
		
		@Override
		public boolean unique() {
			return false;
		}
		
		@Override
		public Class<?> type() {
			return Date.class;
		}
		
		@Override
		public int scale() {
			return 0;
		}

		@Override
		public boolean readOnly() {
			return false;
		}
		
		@Override
		public int precision() {
			return 0;
		}
		
		@Override
		public String name() {
			return "updatedOn";
		}
		
		@Override
		public String init() {
			return "";
		}
		
		@Override
		public boolean indexed() {
			return false;
		}
		
		@Override
		public String check() {
			return "";
		}
	};

}

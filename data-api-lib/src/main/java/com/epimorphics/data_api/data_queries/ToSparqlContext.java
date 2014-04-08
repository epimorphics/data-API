/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

public interface ToSparqlContext {

	void notImplemented(Constraint c);
	
	void comment(String message, Object... values);

	void generateFilter(Filter f);
	
	void generateBelow(Filter f);

	void generateSearch(SearchSpec s);
	
	void nest();

	void unNest();

	Constraint begin(Constraint c);

	void end();

	void union();
}
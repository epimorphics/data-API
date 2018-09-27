package com.epimorphics.data_api.conversions;

import com.epimorphics.appbase.data.ClosableResultSet;
import com.epimorphics.json.JSFullWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;

import java.io.*;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class CountWriterTest {
	private final QuerySolutionMap value = new QuerySolutionMap();
	private final ClosableResultSet rs = mock(ClosableResultSet.class);
	private final CountWriter writer = new CountWriter(rs);
	private final OutputStream stream = new ByteArrayOutputStream();
	private final JSFullWriter output = new JSFullWriter(stream);

	public CountWriterTest() {
		value.add("_count", ResourceFactory.createTypedLiteral(100));
	}

	private String normalise(String json) {
		try {
			return new ObjectMapper().reader().readTree(json).toString();
		} catch (IOException ioe) {
			fail("JSON mapping failed");
			return "";
		}
	}

	@Test
	public void writeTo_resultSetHasResult_writesJson() {
		when(rs.hasNext()).thenReturn(true);
		when(rs.next()).thenReturn(value);

		writer.writeTo(output);
		output.finishOutput();

		String json = normalise(stream.toString());
		String expected = "[{\"@count\":100}]";
		assertEquals(expected, json);

		verify(rs).close();
	}

	@Test
	public void writeTo_resultSetHasNoResults_writesJson() {
		when(rs.hasNext()).thenReturn(false);

		writer.writeTo(output);
		output.finishOutput();

		String json = normalise(stream.toString());
		String expected = "[]";
		assertEquals(expected, json);

		verify(rs).close();
	}
}
/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.loader.jar;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.boot.loader.data.ByteArrayRandomAccessData;
import org.springframework.boot.loader.data.RandomAccessData;
import org.springframework.boot.loader.data.RandomAccessDataFile;

/**
 * A Jar file that can loaded from a {@link RandomAccessDataFile}. This class extends and
 * behaves in the same was a the standard JDK {@link JarFile} the following additional
 * functionality.
 * <ul>
 * <li>Jar entries can be {@link JarEntryFilter filtered} during construction and new
 * filtered files can be {@link #getFilteredJarFile(JarEntryFilter...) created} from
 * existing files.</li>
 * <li>A nested {@link JarFile} can be
 * {@link #getNestedJarFile(ZipEntry, JarEntryFilter...) obtained} based on any directory
 * entry.</li>
 * <li>A nested {@link JarFile} can be
 * {@link #getNestedJarFile(ZipEntry, JarEntryFilter...) obtained} for embedded JAR files
 * (as long as their entry is not compressed).</li>
 * <li>Entry data can be accessed as {@link RandomAccessData}.</li>
 * </ul>
 * 
 * @author Phillip Webb
 */
public class RandomAccessJarFile extends JarFile {

	private static final RandomAccessData EMPTY_DATA = new ByteArrayRandomAccessData(
			new byte[0]);

	private final RandomAccessDataFile rootJarFile;

	private RandomAccessData data;

	private final String name;

	private final long size;

	private Map<String, JarEntry> entries = new LinkedHashMap<String, JarEntry>();

	private Manifest manifest;

	/**
	 * Create a new {@link RandomAccessJarFile} backed by the specified file.
	 * @param file the root jar file
	 * @param filters an optional set of jar entry filters
	 * @throws IOException
	 */
	public RandomAccessJarFile(File file, JarEntryFilter... filters) throws IOException {
		this(new RandomAccessDataFile(file), filters);
	}

	/**
	 * Create a new {@link RandomAccessJarFile} backed by the specified file.
	 * @param file the root jar file
	 * @param filters an optional set of jar entry filters
	 * @throws IOException
	 */
	public RandomAccessJarFile(RandomAccessDataFile file, JarEntryFilter... filters)
			throws IOException {
		this(file, file.getFile().getPath(), file, filters);
	}

	/**
	 * Private constructor used to create a new {@link RandomAccessJarFile} either
	 * directly or from a nested entry.
	 * @param rootJarFile the root jar file
	 * @param name the name of this file
	 * @param data the underlying data
	 * @param filters an optional set of jar entry filters
	 * @throws IOException
	 */
	private RandomAccessJarFile(RandomAccessDataFile rootJarFile, String name,
			RandomAccessData data, JarEntryFilter... filters) throws IOException {
		super(rootJarFile.getFile());
		System.out.println(System.nanoTime());
		this.rootJarFile = rootJarFile;
		this.name = name;
		this.data = data;
		this.size = data.getSize();

		RandomAccessDataJarInputStream inputStream = new RandomAccessDataJarInputStream(
				data);
		try {
			RandomAccessDataJarEntry zipEntry = inputStream.getNextEntry();
			while (zipEntry != null) {
				addJarEntry(zipEntry, filters);
				zipEntry = inputStream.getNextEntry();
			}
			this.manifest = inputStream.getManifest();
			if (this.manifest != null) {
				addManifestEntries(filters);
			}
		}
		finally {
			System.out.println(System.nanoTime());
			inputStream.close();
		}
	}

	private void addManifestEntries(JarEntryFilter... filters) throws IOException {

		Map<String, JarEntry> originalEntries = this.entries;
		this.entries = new LinkedHashMap<String, JarEntry>();

		ZipInputStream zipInputStream = new ZipInputStream(this.data.getInputStream());
		try {
			JarEntry entry;
			do {
				entry = new JarEntry(zipInputStream.getNextEntry());
				entry.setMethod(ZipEntry.STORED);
				RandomAccessData data = EMPTY_DATA;
				if (MANIFEST_NAME.equals(entry.getName())) {
					ByteArrayOutputStream manifestBytes = new ByteArrayOutputStream();
					this.manifest.write(manifestBytes);
					manifestBytes.close();
					data = new ByteArrayRandomAccessData(manifestBytes.toByteArray());
				}
				addJarEntry(new RandomAccessDataJarEntry(entry, data), filters);
			}
			while (!MANIFEST_NAME.equals(entry.getName()));

			this.entries.putAll(originalEntries);
		}
		finally {
			zipInputStream.close();
		}
	}

	private void addJarEntry(RandomAccessDataJarEntry zipEntry, JarEntryFilter... filters) {
		Entry jarEntry = new Entry(zipEntry);
		String name = zipEntry.getName();
		for (JarEntryFilter filter : filters) {
			name = (filter == null || name == null ? name : filter.apply(name, jarEntry));
		}
		if (name != null) {
			jarEntry.setName(name);
			this.entries.put(name, jarEntry);
		}
	}

	protected final RandomAccessDataFile getRootJarFile() {
		return this.rootJarFile;
	}

	@Override
	public Manifest getManifest() throws IOException {
		return this.manifest;
	}

	@Override
	public Enumeration<JarEntry> entries() {
		return Collections.enumeration(this.entries.values());
	}

	@Override
	public JarEntry getJarEntry(String name) {
		return (JarEntry) getEntry(name);
	}

	@Override
	public ZipEntry getEntry(String name) {
		JarEntry entry = this.entries.get(name);
		if (entry == null && name != null && !name.endsWith("/")) {
			entry = this.entries.get(name + "/");
		}
		return entry;
	}

	@Override
	public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
		InputStream inputStream = getData(ze).getInputStream();
		if (ze.getMethod() == ZipEntry.DEFLATED) {
			inputStream = new ZipInflaterInputStream(inputStream, (int) ze.getSize());
		}
		return inputStream;
	}

	/**
	 * Return a nested {@link RandomAccessJarFile} loaded from the specified entry.
	 * @param ze the zip entry
	 * @param filters an optional set of jar entry filters to be applied
	 * @return a {@link RandomAccessJarFile} for the entry
	 * @throws IOException
	 */
	public synchronized RandomAccessJarFile getNestedJarFile(final ZipEntry ze,
			JarEntryFilter... filters) throws IOException {
		if (ze == null) {
			throw new IllegalArgumentException("ZipEntry must not be null");
		}

		if (ze.isDirectory()) {
			return getNestedJarFileFromDirectoryEntry(ze, filters);
		}

		return getNestedJarFileFromFileEntry(ze, filters);
	}

	private RandomAccessJarFile getNestedJarFileFromDirectoryEntry(final ZipEntry entry,
			JarEntryFilter... filters) throws IOException {
		final String name = entry.getName();
		JarEntryFilter[] filtersToUse = new JarEntryFilter[filters.length + 1];
		System.arraycopy(filters, 0, filtersToUse, 1, filters.length);
		filtersToUse[0] = new JarEntryFilter() {
			@Override
			public String apply(String entryName, JarEntry ze) {
				if (entryName.startsWith(name) && !entryName.equals(name)) {
					return entryName.substring(entry.getName().length());
				}
				return null;
			}
		};
		return new RandomAccessJarFile(this.rootJarFile, getName() + "!/"
				+ name.substring(0, name.length() - 1), this.data, filtersToUse);
	}

	private RandomAccessJarFile getNestedJarFileFromFileEntry(ZipEntry entry,
			JarEntryFilter... filters) throws IOException {
		if (entry.getMethod() != ZipEntry.STORED) {
			throw new IllegalStateException("Unable to open nested compressed entry "
					+ entry.getName());
		}
		return new RandomAccessJarFile(this.rootJarFile, getName() + "!/"
				+ entry.getName(), getData(entry), filters);
	}

	/**
	 * Return a new jar based on the filtered contents of this file.
	 * @param filters the set of jar entry filters to be applied
	 * @return a filtered {@link RandomAccessJarFile}
	 * @throws IOException
	 */
	public synchronized RandomAccessJarFile getFilteredJarFile(JarEntryFilter... filters)
			throws IOException {
		return new RandomAccessJarFile(this.rootJarFile, getName(), this.data, filters);
	}

	/**
	 * Return {@link RandomAccessData} for the specified entry.
	 * @param ze the zip entry
	 * @return the entry {@link RandomAccessData}
	 * @throws IOException
	 */
	private synchronized RandomAccessData getData(ZipEntry ze) throws IOException {
		if (!this.entries.containsValue(ze)) {
			throw new IllegalArgumentException("ZipEntry must be contained in this file");
		}
		return ((Entry) ze).getData();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int size() {
		return (int) this.size;
	}

	@Override
	public void close() throws IOException {
		this.rootJarFile.close();
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Return a URL that can be used to access this JAR file. NOTE: the specified URL
	 * cannot be serialized and or cloned.
	 * @return the URL
	 * @throws MalformedURLException
	 */
	public URL getUrl() throws MalformedURLException {
		RandomAccessJarURLStreamHandler handler = new RandomAccessJarURLStreamHandler(
				this);
		return new URL("jar", "", -1, "file:" + getName() + "!/", handler);
	}

	/**
	 * A single {@link JarEntry} in this file.
	 */
	private static class Entry extends JarEntry {

		private String name;

		private RandomAccessData entryData;

		public Entry(RandomAccessDataJarEntry entry) {
			super(entry);
			this.entryData = entry.getData();
		}

		void setName(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return (this.name == null ? super.getName() : this.name);
		}

		public RandomAccessData getData() {
			return this.entryData;
		}
	}

	/**
	 * {@link URLStreamHandler} used to support {@link RandomAccessJarFile#getUrl()}.
	 */
	private static class RandomAccessJarURLStreamHandler extends URLStreamHandler {

		private RandomAccessJarFile jarFile;

		public RandomAccessJarURLStreamHandler(RandomAccessJarFile jarFile) {
			this.jarFile = jarFile;
		}

		@Override
		protected URLConnection openConnection(URL url) throws IOException {
			return new RandomAccessJarURLConnection(url, this.jarFile);
		}
	}

	/**
	 * {@link JarURLConnection} used to support {@link RandomAccessJarFile#getUrl()}.
	 */
	private static class RandomAccessJarURLConnection extends JarURLConnection {

		private RandomAccessJarFile jarFile;

		private JarEntry jarEntry;

		private String jarEntryName;

		private String contentType;

		protected RandomAccessJarURLConnection(URL url, RandomAccessJarFile jarFile)
				throws MalformedURLException {
			super(new URL("jar:file:" + jarFile.getRootJarFile().getFile().getPath()
					+ "!/"));
			this.jarFile = jarFile;

			String spec = url.getFile();
			int separator = spec.lastIndexOf("!/");
			if (separator == -1) {
				throw new MalformedURLException("no !/ found in url spec:" + spec);
			}
			if (separator + 2 != spec.length()) {
				this.jarEntryName = spec.substring(separator + 2);
			}
		}

		@Override
		public void connect() throws IOException {
			if (this.jarEntryName != null) {
				this.jarEntry = this.jarFile.getJarEntry(this.jarEntryName);
				if (this.jarEntry == null) {
					throw new FileNotFoundException("JAR entry " + this.jarEntryName
							+ " not found in " + this.jarFile.getName());
				}
			}
			this.connected = true;
		}

		@Override
		public RandomAccessJarFile getJarFile() throws IOException {
			connect();
			return this.jarFile;
		}

		@Override
		public JarEntry getJarEntry() throws IOException {
			connect();
			return this.jarEntry;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			connect();
			if (this.jarEntryName == null) {
				throw new IOException("no entry name specified");
			}
			return this.jarFile.getInputStream(this.jarEntry);
		}

		@Override
		public int getContentLength() {
			try {
				connect();
				return (int) (this.jarEntry == null ? this.jarFile.size() : this.jarEntry
						.getSize());
			}
			catch (IOException ex) {
				return -1;
			}
		}

		@Override
		public Object getContent() throws IOException {
			connect();
			return (this.jarEntry == null ? this.jarFile : super.getContent());
		}

		@Override
		public String getContentType() {
			if (this.contentType == null) {
				// Guess the content type, don't bother with steams as mark is not
				// supported
				this.contentType = (this.jarEntryName == null ? "x-java/jar" : null);
				this.contentType = (this.contentType == null ? guessContentTypeFromName(this.jarEntryName)
						: this.contentType);
				this.contentType = (this.contentType == null ? "content/unknown"
						: this.contentType);
			}
			return this.contentType;
		}
	}

	/**
	 * {@link InflaterInputStream} that supports the writing of an extra "dummy" byte
	 * (which is required with JDK 6) and returns accurate available() results.
	 */
	private static class ZipInflaterInputStream extends InflaterInputStream {

		private boolean extraBytesWritten;

		private int available;

		public ZipInflaterInputStream(InputStream inputStream, int size) {
			super(inputStream, new Inflater(true), 512);
			this.available = size;
		}

		@Override
		public int available() throws IOException {
			if (this.available < 0) {
				return super.available();
			}
			return this.available;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int result = super.read(b, off, len);
			if (result != -1) {
				this.available -= result;
			}
			return result;
		}

		@Override
		protected void fill() throws IOException {
			try {
				super.fill();
			}
			catch (EOFException ex) {
				if (this.extraBytesWritten) {
					throw ex;
				}
				this.len = 1;
				this.buf[0] = 0x0;
				this.extraBytesWritten = true;
				this.inf.setInput(this.buf, 0, this.len);
			}
		}

	}

}

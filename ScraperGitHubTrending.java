package com.hbh.test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 爬取Github上每日热门的开源项目
 * @author hbh
 */
public class ScraperGitHubTrending {

	// 网站域名
	private static final String SERVER_WEB = "https://github.com";
	// 待爬取链接
	private static final String URL = new String("https://github.com/trending/");
	// 需要获取数据的语言
	private static final String[] LANGUAGES = new String[]{
			"Go",
			"Java",
			"Python"
	};
	// 目录名
	private static final String FILE_HOME_DIR = "f://";
	// Http请求头部
	private static final List<BasicHeader> HEADERS = new ArrayList<>();

	static {
		HEADERS.add(new BasicHeader("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:11.0) Gecko/20100101 Firefox/11.0"));
		HEADERS.add(new BasicHeader("Accept-Encoding", "gzip,deflate,sdch"));
		HEADERS.add(new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
		HEADERS.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3"));
	}

	private static BasicHeader[] getHeaders() {
		return HEADERS.toArray(new BasicHeader[HEADERS.size()]);
	}

	/**
	 * 解析器 提取信息
	 * @param html 纯文字的html页面
	 * @return
	 */
	public static void parse(String html, String language) {
		append("\n##" + language + "\n");
		// 解析为文档
		Document dom = Jsoup.parse(html);
		// 获取li标签的元素
		Elements elements = dom.select("ol.repo-list li");
		for (Element element : elements) {
			// String owner = element.select("span.prefix").text();
			StringBuffer data = new StringBuffer();
			// MarkDown语法 创建无序列表中的一条记录
			data.append("* [").append(element.select("h3 a").text()).append("](")
					.append(SERVER_WEB + element.select(("h3 a")).attr("href")).append("):")
					.append(element.select("p.col-9").text()).append("\n");
			append(data.toString());
		}
	}

	/**
	 * 创建文件
	 * @param context 文件需要追加的内容
	 */
	public static void append(String context) {
		String fileName = FILE_HOME_DIR
				+ new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()) + ".md";
		File file = new File(fileName);
		if (fileIsNotExists(file)) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.print("创建文件的时候出问题啦");
			}
		}
		try (RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw")){
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.write(context.getBytes());
		} catch (IOException e) {
			System.out.print("写入内容的时候出问题啦");
		}
	}

	/**
	 * 判断文件是否不存在
	 * @param file
	 * @return
	 */
	private static boolean fileIsNotExists(File file) {
		return !file.exists();
	}
	
	/**
	 * 判断状态码是否正确
	 * @param response
	 * @return
	 */
	private static boolean statusIsTure(HttpResponse response) {
		return response.getStatusLine().getStatusCode() == 200;
	}
	
	public static void main(String[] args) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		for (String language : LANGUAGES) {
			HttpGet get = new HttpGet(URL + language);
			get.setHeaders(getHeaders());
			
			HttpResponse response = httpClient.execute(get);
			assert(statusIsTure(response));
			HttpEntity entity = response.getEntity();
			parse(EntityUtils.toString(entity, "utf-8"), language);
			EntityUtils.consume(entity);
		}
		httpClient.close();
	}
	
	
}

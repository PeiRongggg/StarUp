package _00_init.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import _01_register.dao.UserDao;
import _01_register.dao.impl.UserDaoImpl;
import _01_register.model.UserBean;


// 
// 程式功能：
// 本Servlet 類別會依據傳入的主鍵呼叫Service元件以讀取該主鍵所對應的紀錄，取出該紀錄內的BLOB欄，
// 進而讀取存放在BLOB欄內的圖片資料，然後傳回給提出請求的瀏覽器。

@WebServlet("/Util/getImage")
public class RetrieveImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		OutputStream os = null;
		InputStream is = null;
		String fileName = null;
		try {
			// 讀取瀏覽器傳送來的主鍵
			String id = request.getParameter("id");
			System.out.println("id = "+id);
			// 讀取瀏覽器傳送來的type，以分辨要處理哪個表格
			String type = request.getParameter("type"); 
			switch(type.toUpperCase()){
				case "USER":
//					UserDao userDao = new UserDaoImpl();
					ServletContext sc = getServletContext();
					WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(sc);
					UserDaoImpl userDao = ctx.getBean(UserDaoImpl.class);
					int nId = 0;
					try {
						nId = Integer.parseInt(id);
					} catch(NumberFormatException ex) {
						ex.printStackTrace();;
					}
					UserBean bean1 = userDao.getUser(nId);
					System.out.println("UserBean = "+bean1);
					is = bean1.getPhoto().getBinaryStream();
					fileName = bean1.getPhotoName();
					System.out.println("File name = "+fileName);
					break;
//				case "Product":
//					ProductService productService = new ProductServiceImpl();
//					ProductBean bean2 = productService.queryProduct(id);
//					is = bean2.getProductImage().getBinaryStream();  
//					fileName = bean2.getFileName();
			}

			// 由圖片檔的檔名來得到檔案的MIME型態
			String mimeType = getServletContext().getMimeType(fileName);
			System.out.println("mimeType = "+mimeType);
			// 設定輸出資料的MIME型態
			response.setContentType(mimeType);
			// 取得能寫出非文字資料的OutputStream物件
						
			// 如果圖片的來源有問題，就送回預設圖片(/images/NoImage.jpg)	
			if (is == null) {
				is = getServletContext().getResourceAsStream(
							"/images/NoImage.jpg");
			}
			os = response.getOutputStream();	
			System.out.println("準備讀出照片");
			// 由InputStream讀取位元組，然後由OutputStream寫出
			int len = 0;
			byte[] bytes = new byte[8192];
			while ((len = is.read(bytes)) != -1) {
				os.write(bytes, 0, len);
			}
		} catch(SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException("Util.RetrieveImageServlet#doGet()發生SQLException: " + ex.getMessage());
		} finally{
			is.close();
			os.close();
		}
	}
}
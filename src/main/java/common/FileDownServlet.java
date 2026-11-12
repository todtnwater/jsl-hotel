package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 첨부파일 다운로드 서블릿
 * - 공지사항(notice) 첨부파일만 다운로드 허용
 * - 파일 이름의 경로 조작(../, /, \) 차단
 */
@WebServlet("/FileDownServlet")
public class FileDownServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/** 다운로드를 허용하는 폴더 구분값 */
	private static final String DIR_NOTICE = "notice";

	/** 전송 버퍼 크기 (8KB) */
	private static final int BUFFER_SIZE = 8192;

	public FileDownServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String savePath = request.getParameter("t_fileDir");
		String fileName = request.getParameter("t_fileName");

		// 1. 허용된 폴더만 다운로드 가능 (현재는 공지사항만 사용)
		if (!DIR_NOTICE.equals(savePath)) {
			sendAlert(response, HttpServletResponse.SC_BAD_REQUEST, "잘못된 요청입니다.");
			return;
		}

		// 2. 파일 이름 검사 (경로 이동 문자, 제어 문자 차단)
		if (!isSafeFileName(fileName)) {
			sendAlert(response, HttpServletResponse.SC_BAD_REQUEST, "잘못된 파일 이름입니다.");
			return;
		}

		// 3. 최종 경로가 공지사항 폴더 바로 아래인지 다시 확인
		File baseDir = new File(CommonUtil.getNoticeDir(request)).getCanonicalFile();
		File file = new File(baseDir, fileName).getCanonicalFile();
		File parentDir = file.getParentFile();
		if (parentDir == null || !parentDir.equals(baseDir)) {
			sendAlert(response, HttpServletResponse.SC_BAD_REQUEST, "잘못된 파일 경로입니다.");
			return;
		}

		// 4. 로컬에 파일이 없으면 SFTP에서 받아오기
		if (!file.exists()) {
			boolean downloadSuccess = CommonUtil.downloadNoticeFromSFTP(fileName, file.getPath());
			if (!downloadSuccess || !file.exists()) {
				sendAlert(response, HttpServletResponse.SC_NOT_FOUND, "파일을 다운로드할 수 없습니다.");
				return;
			}
		}

		// 5. 파일 전송 (한글 파일 이름은 UTF-8로 인코딩)
		String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

		response.reset();
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);
		response.setHeader("Content-Length", String.valueOf(file.length()));
		response.setHeader("X-Content-Type-Options", "nosniff");

		try (InputStream in = new FileInputStream(file);
			 OutputStream os = response.getOutputStream()) {
			byte[] buffer = new byte[BUFFER_SIZE];
			int len;
			while ((len = in.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			os.flush();
		} catch (IOException e) {
			System.out.println("첨부 파일 다운 오류~ 파일명:" + fileName);
			e.printStackTrace();
		} finally {
			// 다운로드 후 임시로 받아 둔 공지사항 파일 삭제
			if (file.exists() && !file.delete()) {
				System.out.println("임시 파일 삭제 실패: " + file.getPath());
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * 파일 이름이 안전한지 검사
	 * - 비어 있거나 너무 길면 거부
	 * - 경로 구분자(/, \)나 상위 폴더 이동(..)이 있으면 거부
	 * - 제어 문자가 있으면 거부
	 */
	private static boolean isSafeFileName(String name) {
		if (name == null || name.isEmpty() || name.length() > 255) {
			return false;
		}
		if (name.contains("/") || name.contains("\\") || name.contains("..")) {
			return false;
		}
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c < 0x20 || c == 0x7f) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 오류 메시지를 알림창으로 보여 주고 이전 페이지로 돌아가기
	 * - 메시지는 코드 안의 고정 문자열만 사용 (사용자 입력을 넣지 않음)
	 */
	private static void sendAlert(HttpServletResponse response, int status, String message) throws IOException {
		response.reset();
		response.setStatus(status);
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<script>alert('" + message + "');history.back();</script>");
		out.flush();
	}
}

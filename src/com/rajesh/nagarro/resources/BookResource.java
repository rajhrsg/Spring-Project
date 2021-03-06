package com.rajesh.nagarro.resources;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import com.rajesh.nagarro.constants.GlobalConstants;
import com.rajesh.nagarro.dao.UserDao;
import com.rajesh.nagarro.model.Book;
import com.rajesh.nagarro.model.IssuedBook;
import com.rajesh.nagarro.model.common.Message;
import com.rajesh.nagarro.utils.DateTimeUtils;
import com.rajesh.nagarro.utils.FileUtils;
import com.rajesh.nagarro.utils.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;


@Path("books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public final class BookResource {

	@Autowired
	UserDao userDao;

	@HeaderParam("token")
	int token;

	private Logger logger = Logger.getLogger(BookResource.class);

	@Autowired
	private ServletContext servletContext;

	public BookResource() {
		userDao = SpringUtils.getUserDao();
	}

	/**
	 * @param book
	 * @return
	 */
	@POST
	public Response addBook(Book book) {
		Message message = null;
		logger.info("Book category " + book);
		boolean result = userDao.addBook(book);
		if (result) {
			message = new Message(true, GlobalConstants.ADD_SUCCESS, null);
		} else {
			message = new Message(false, GlobalConstants.ADD_FAULURE, null);
		}
		return Response.status(Status.OK).entity(message).build();
	}

	/**
	 * @param categoryId
	 * @return list of books by categoryId
	 */
	@GET
	@Path("{categoryId}")
	public Response getBooksByCategoryId(@PathParam("categoryId") int categoryId) {
		Message message = null;
		logger.debug("getBooksByCategoryId : CategoryId " + categoryId);
		List<Book> list = userDao.getBooksByCategoryId(categoryId);
		if (list == null) {
			message = new Message(false, GlobalConstants.NO_RECORD, null);
		} else {
			message = new Message(true, null, list);
		}
		return Response.status(Status.OK).entity(message).build();
	}

	/**
	 * @param categoryId
	 * @return
	 */
	@GET
	@Path("categories")
	public Response getBooksByCategory(@QueryParam("categoryId") int categoryId) {
		Message message = null;
		logger.debug("getBooksByCategoryId : CategoryId " + categoryId);
		List<Book> list = userDao.getBooksByCategoryId(categoryId);
		if (list == null) {
			message = new Message(false, GlobalConstants.NO_RECORD, null);
		} else {
			message = new Message(true, null, list);
		}
		return Response.status(Status.OK).entity(message).build();
	}

	/**
	 * @param title
	 * @param author
	 * @param publication
	 * @param category
	 * @return
	 */
	@GET
	public Response getAllBooks(@QueryParam("title") String title, @QueryParam("author") String author,
			@QueryParam("publication") String publication, @QueryParam("category") String category) {
		Message message = null;

		title = title == null ? "" : title;
		author = author == null ? "" : author;
		publication = publication == null ? "" : publication;
		category = category == null ? "" : category;

		logger.info("Title " + title);
		logger.info("author " + author);
		logger.info("publication " + publication);
		logger.info("category " + category);

		List<Book> list = userDao.searchBooks(title, author, publication, category);

		if (list == null) {
			message = new Message(false, GlobalConstants.NO_RECORD, null);
		} else {
			message = new Message(true, null, list);
		}
		return Response.status(Status.OK).entity(message).build();
	}

	/**
	 * @param categoryId
	 * @param id
	 * @return
	 */
	@GET
	@Path("{categoryId}/{id}")
	public Response geBookById(@PathParam("categoryId") int categoryId, @PathParam("id") int id) {
		Message message = null;
		logger.info("categoryId " + categoryId);
		logger.info("bookId " + id);
		Book object = userDao.getBookById(categoryId, id);
		if (object == null) {
			message = new Message(false, GlobalConstants.NO_RECORD, null);
		} else {
			message = new Message(true, null, object);
		}
		return Response.status(Status.OK).entity(message).build();
	}

	/**
	 * @return
	 */
	@GET
	@Path("/issued")
	public Response geIssuedBooks() {
		Message message = null;
		List<IssuedBook> object = userDao.getIssuedBooksByUserId(token);
		if (object == null) {
			message = new Message(false, GlobalConstants.NO_RECORD, null);
		} else {
			message = new Message(true, null, object);
		}
		return Response.status(Status.OK).entity(message).build();

	}

	/**
	 * @param id
	 * @param uploadedInputStream
	 * @param fileDetail
	 * @return
	 */
	@POST
	@Path("/update/{id}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadProfilePic(@PathParam("id") int id, @FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
		Message message = null;
		String fileName = fileDetail.getFileName();
		String fileExtension = FileUtils.getFileExtension(fileName);

		if (FileUtils.isValidFileExtension(fileExtension)) {

			String finalName = DateTimeUtils.getCurrentDateTimeForFile().concat(".").concat(fileExtension);
			String BASEPATH = servletContext.getRealPath("") + File.separator + GlobalConstants.BOOK_DIR;
			logger.debug(BASEPATH + "/" + finalName);
			if (FileUtils.writeToFile(uploadedInputStream, BASEPATH, fileName, finalName)) {
				logger.debug("Image uploaded successfully.");
				if (userDao.updateBookImage(id, finalName)) {
					logger.debug("updateBookImage successfully.");
					message = new Message(true, GlobalConstants.UPDATE_SUCCESS, finalName);
				} else {
					logger.debug("updateBookImage failed.");
					message = new Message(false, GlobalConstants.UPDATE_FAILURE, null);
				}
			} else {
				message = new Message(false, GlobalConstants.UPDATE_FAILURE, null);
			}
		} else {
			message = new Message(false, GlobalConstants.INVALID_FILE_FORMAT + " " + GlobalConstants.IMAGE_ALLOWED,
					null);
		}
		return Response.status(Status.OK).entity(message).build();
	}

	/**
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("{id}")
	public Response deleteBook(@PathParam("id") int id) {
		Message message = null;
		if (id > 0) {
			if (userDao.deleteBook(id)) {
				message = new Message(true, GlobalConstants.DELETE_SUCCESS, null);
			} else {
				message = new Message(false, GlobalConstants.DELETE_FAILURE, null);
			}
		} else {
			message = new Message(false, "Invalid Book Id", null);
		}
		return Response.status(Status.OK).entity(message).build();
	}
}
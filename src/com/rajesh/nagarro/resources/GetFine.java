package com.rajesh.nagarro.resources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;
import com.rajesh.nagarro.dao.UserDao;
import com.rajesh.nagarro.model.Fine;
import com.rajesh.nagarro.model.common.Message;
import org.springframework.beans.factory.annotation.Autowired;

@Path("fine_amount")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GetFine {

	@Autowired
	UserDao userDao;

	private Logger logger = Logger.getLogger(GetFine.class);

	/**
	 * @param bookIssueId
	 * @return
	 */
	@GET
	public Response getFine(@QueryParam("bookIssueId") int bookIssueId) {
		logger.info(" Fine " + bookIssueId);
		Message message = null;
		List<Fine> result = userDao.getFine(bookIssueId);
		if (result != null) {
			message = new Message(true, "This is your Fine.", result);
		} else {
			message = new Message(false, "Error in Fine", null);
		}
		return Response.status(Status.OK).entity(message).build();
	}
}
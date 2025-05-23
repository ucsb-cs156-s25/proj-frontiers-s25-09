import React from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "react-bootstrap";
import { Table } from "react-bootstrap";
import { useCurrentUser } from "main/utils/currentUser";

export default function RosterStudentsTable({ rosterStudents, courseId }) {
  const navigate = useNavigate();
  const { currentUser } = useCurrentUser();

  return (
    <>
      {hasRole(currentUser, "ROLE_ADMIN") && (
        <Button 
          variant="primary" 
          onClick={() => navigate(`/admin/courses/${courseId}/roster_students/new`)}
          style={{ marginBottom: "1em" }}
        >
          New Roster Student
        </Button>
      )}
      
      <Table striped bordered hover>
        <thead>
          <tr>
            <th>Student ID</th>
            <th>First Name</th>
            <th>Last Name</th>
            <th>Email</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {rosterStudents.map(student => (
            <tr key={student.id}>
              <td>{student.studentId}</td>
              <td>{student.firstName}</td>
              <td>{student.lastName}</td>
              <td>{student.email}</td>
              <td>
                <Button 
                  variant="warning"
                  onClick={() => navigate(`/admin/courses/${courseId}/roster_students/${student.id}/edit`)}
                  style={{ marginRight: "0.5em" }}
                >
                  Edit
                </Button>
                <Button 
                  variant="danger"
                  onClick={() => {/* Add delete logic */}}
                >
                  Delete
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </Table>
    </>
  );
}
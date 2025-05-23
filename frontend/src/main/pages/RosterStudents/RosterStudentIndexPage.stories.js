import { useParams } from "react-router-dom";
import { useBackend } from "main/utils/useBackend";
import RosterStudentsTable from "main/components/RosterStudents/RosterStudentsTable";
import BasicLayout from "main/layouts/BasicLayout/BasicLayout";

export default function RosterStudentIndexPage() {
  const { courseId } = useParams();

  const { data: rosterStudents, error: _error, status: _status } =
    useBackend(
      [`/api/rosterstudents/course?courseId=${courseId}`],
      { method: "GET", url: `/api/rosterstudents/course?courseId=${courseId}` },
      []
    );

  return (
    <BasicLayout>
      <div className="pt-2">
        <h1>Course Roster</h1>
        <RosterStudentsTable rosterStudents={rosterStudents} courseId={courseId} />
      </div>
    </BasicLayout>
  );
}
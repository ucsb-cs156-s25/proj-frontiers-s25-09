export default {
  title: "components/RosterStudents/RosterStudentsTable",
  component: RosterStudentsTable,
};

const Template = (args) => <RosterStudentsTable {...args} />;

export const Empty = Template.bind({});
Empty.args = {
  rosterStudents: [],
  courseId: "1"
};

export const Populated = Template.bind({});
Populated.args = {
  rosterStudents: [
    {
      id: 1,
      studentId: "12345",
      firstName: "Chris",
      lastName: "Gaucho",
      email: "cg@ucsb.edu"
    }
  ],
  courseId: "1"
};
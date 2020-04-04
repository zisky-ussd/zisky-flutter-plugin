class ActionResponse {
  String message;
  String status;
  Map<String, dynamic> parsed_variables;

  ActionResponse({this.message, this.status, this.parsed_variables});

  ActionResponse.fromJson(Map<String, dynamic> json) {
    message = json['message'];
    status = json['status'];
    parsed_variables = json['parsed_variables'] == null
        ? new Map()
        : json['parsed_variables'] as Map<String, dynamic>;
  }
}

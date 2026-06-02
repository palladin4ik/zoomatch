import mimetypes


def get_content_type(file_name):
    content_type, _ = mimetypes.guess_type(file_name)

    return content_type or 'application/octet-stream'

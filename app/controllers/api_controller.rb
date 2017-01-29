class ApiController < ActionController::Base
  # protect_from_forgery with: :exception
  before_action :authorize!

  protected

  def current_user
    @current_user ||= User.find_by_token(params[:token])
  end

  def authorize!
    if current_user.nil?
      render json: {error: "No active session"}
    end
  end
end
